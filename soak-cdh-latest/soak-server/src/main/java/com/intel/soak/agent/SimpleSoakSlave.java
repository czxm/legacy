package com.intel.soak.agent;

import com.intel.bigdata.common.protocol.Payload;
import com.intel.soak.gauge.GaugeSlave;
import com.intel.soak.logger.TransactionLogger;
import com.intel.soak.agent.gauge.AgentDriverLogger;
import com.intel.soak.agent.gauge.AgentTransactionLogger;
import com.intel.soak.config.ConfigUtils;
import com.intel.soak.config.SoakConfig;
import com.intel.soak.transaction.TransactionMetrics;
import com.intel.soak.utils.LoadUtils;
import com.intel.soak.vuser.VUserExecutor;
import com.intel.soak.driver.IDriver;
import com.intel.soak.model.LoadConfig;
import com.intel.soak.model.ParamType;
import com.intel.soak.model.TransactionType;
import com.intel.soak.plugin.Plugins;
import com.intel.soak.plugin.dto.PluginInfo;
import com.intel.soak.protocol.SoakRequest;
import com.intel.soak.transaction.Transaction;
import com.intel.soak.utils.ThreadUtils;
import com.intel.soak.vuser.VUserData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author xzhan27
 *
 */
@Component("simpleSoakSlave")
@Scope("prototype")
@Lazy(true)
public class SimpleSoakSlave extends AbstractAppSlave{
    protected static Logger LOG = LoggerFactory.getLogger(SimpleSoakSlave.class);

    private String jobId;
    private IDriver driver;
    private boolean driverStarted;
    private LoadConfig loadConfig;
    private ExecutorService executor = Executors.newCachedThreadPool();
    private ConcurrentLinkedQueue<VUserExecutor> vusers = new ConcurrentLinkedQueue<VUserExecutor>();
    private String nodeId;
    
    @Autowired
    @Qualifier("plugins")
    private Plugins plugins;

    @Autowired
    private SoakConfig soakConfig;

    @Autowired
    @Qualifier("simpleGaugeSlave")
    private GaugeSlave gauge;

    @Override
    public Object onReceive(Payload payload) {
        try{
            if(payload instanceof SoakRequest){
                SoakRequest request = (SoakRequest)payload;
                switch(request.getType()){
                    case Initialize:
                        this.loadConfig = request.getItem(SoakRequest.RequestKey.Config);
                        this.jobId = loadConfig.getName();
                        plugins.loadAndRegisterPlugins(loadConfig.getName(),
                                (List<PluginInfo>)request.getItem(SoakRequest.RequestKey.Plugins),
                                ConfigUtils.getDriverParam(loadConfig, "CLASSPATH"));
                        return new Boolean(setupDriver());
                    case Finalize:
                        executor.shutdownNow();
                        if(driverStarted)
                            driver.shutdown();
                        break;
                    case CreateVUser:
                        nodeId = request.getItem(SoakRequest.RequestKey.Node);
                        return new Boolean(createVirtualUser((VUserData)request.getItem(SoakRequest.RequestKey.VUserData)));
                    case KillVUsers:
                        stop();
                        break;
                    case CheckPoint:
                        onCheckPoint((Long)request.getItem(SoakRequest.RequestKey.Timestamp));
                }
            }
        }
        catch(Exception e){
            LOG.error(e.getMessage(), e);
        }
        return new Boolean(false);
    }

    public void stop(){
        for(VUserExecutor vuser : vusers){
            vuser.kill();
        }
        LOG.info("Waiting for all VUsers shutdown");
        while (true) {
            try {
                if(vusers.size() > 0)
                    ThreadUtils.sleep(5);
                else
                    break;
            } catch (Throwable t) {
                LOG.error(t.getMessage(), t);
            }
        }
        plugins.destroy(jobId);
    }

    private void userFinished(){
        postRequest(new SoakRequest(jobId)
                .setType(SoakRequest.RequestType.VUserFinished)
                .setItem(SoakRequest.RequestKey.Node, nodeId)
                .setItem(SoakRequest.RequestKey.VUsers, 1));
    }

    public void onCheckPoint(Long timestamp){
        LOG.info("CheckPoint: " + timestamp);

        TransactionMetrics metrics = null;
        for(VUserExecutor vuser : vusers){
            if(metrics == null){
                metrics = new TransactionMetrics(vuser.getMetricsData());
                metrics.setSource(ConfigUtils.getDriverName(loadConfig) + "_" + nodeId);
                metrics.setTimestamp(timestamp);
                metrics.merge(vuser.getMetricsData());
            }
            else{
                metrics.merge(vuser.getMetricsData());
            }
        }
        if(metrics != null){
            metrics.commit();
            gauge.sendMetrics(loadConfig.getName(), new TransactionMetrics[]{metrics});
        }
    }

    protected boolean setupDriver(){
        driver = LoadUtils.callPluginCallable(executor, loadConfig.getName(), plugins,
                new Callable<IDriver>(){
                    @Override
                    public IDriver call(){
                        return LoadUtils.createDriver(plugins, loadConfig);
                    }
                });
        return driver != null;
    }

    protected boolean startDriver(){
        try{
            if(!driverStarted){
                final AgentDriverLogger logger = LoadUtils.createLogger(SoakConfig.DrvLogger, AgentDriverLogger.class);
                logger.setComponent(driver.getClass().getName());
                logger.setSource(nodeId);
                final List<ParamType> params = LoadUtils.cloneParams(ConfigUtils.getDriverParams(loadConfig));
                LoadUtils.prepareParams(params, plugins, jobId, null);
                if(!LoadUtils.callPluginCallable(executor, loadConfig.getName(), plugins,
                        new Callable<Boolean>() {
                            @Override
                            public Boolean call() throws Exception {
                                driver.setParams(params);
                                driver.setLogger(logger);
                                return driver.startup();
                            }
                        })){
                    LOG.error("Failed to startup Driver: " + driver.getClass().getName());
                    return false;
                }
                driverStarted = true;
            }
            return true;
        }
        catch(Throwable t){
            LOG.error(t.getMessage(), t);
        }
        return false;
    }
    
    protected boolean createVirtualUser(final VUserData data) {
        try {
            if(!startDriver())
                return false;

            final List<TransactionType> transTypes = ConfigUtils.getTransactions(loadConfig);
            final List<TransactionLogger> loggers = LoadUtils.createLoggers(transTypes.size());

            LoadUtils.invokePluginCallable(executor, loadConfig.getName(), plugins, new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    List<Transaction> transactions = LoadUtils.createTransactions(plugins, loadConfig,
                            data, loggers, transTypes, driver);

                    for(int i = 0; i < loggers.size(); i++){
                        AgentTransactionLogger logger = (AgentTransactionLogger)loggers.get(i);
                        Transaction tran = transactions.get(i);
                        logger.setSource(nodeId);
                        logger.setComponent(tran.getClass().getName());
                    }
                    VUserExecutor vuser = new VUserExecutor(loadConfig, loggers, transactions);
                    vusers.add(vuser);
                    LOG.info(String.format("Started Virtual User: %s", data.getUsername()));
                    vuser.run();
                    userFinished();
                    vusers.remove(vuser);
                    LOG.info(String.format("Virtual User %s finished", data.getUsername()));
                    return true;
                }
            });
        } catch (Throwable e) {
            LOG.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    public void setPlugins(Plugins plugins) {
        this.plugins = plugins;
    }
}
