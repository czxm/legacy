package com.intel.soak.agent;

import com.intel.soak.config.ConfigUtils;
import com.intel.soak.config.SoakConfig;
import com.intel.soak.driver.IDriver;
import com.intel.soak.gauge.GaugeSlave;
import com.intel.soak.logger.DriverLogger;
import com.intel.soak.logger.TransactionLogger;
import com.intel.soak.master.LocalSoakMaster;
import com.intel.soak.model.LoadConfig;
import com.intel.soak.model.ParamType;
import com.intel.soak.model.TransactionType;
import com.intel.soak.plugin.Plugins;
import com.intel.soak.transaction.Transaction;
import com.intel.soak.transaction.TransactionMetrics;
import com.intel.soak.utils.LoadUtils;
import com.intel.soak.utils.ThreadUtils;
import com.intel.soak.vuser.VUserData;
import com.intel.soak.vuser.VUserExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author xzhan27
 *
 */
public class LocalSoakSlave {
    protected static Logger LOG = LoggerFactory.getLogger(LocalSoakSlave.class);
    
    private LocalSoakMaster master;
    private GaugeSlave gauge;
    private volatile boolean stopFlag; 
    private IDriver driver;
    private boolean driverStarted;
    private LoadConfig loadConfig;
    private ExecutorService executor = Executors.newCachedThreadPool();
    private ConcurrentLinkedQueue<VUserExecutor> vusers = new ConcurrentLinkedQueue<VUserExecutor>();
    private long nextCheckPoint;
    private boolean started = false;
    private SoakConfig soakConfig;

    private Plugins plugins;

    public void setMaster(LocalSoakMaster master){
        this.master = master;
    }
    
    public void setSoakConfig(SoakConfig config){
        this.soakConfig = config;
    }
    
    public void start(){    
        stopFlag = false;
        started = true;
        executor.submit(new Runnable(){
            @Override
            public void run() {
                try{
                    while(!stopFlag){
                        nextCheckPoint = master.getNextCheckPoint();
                        long wait = (nextCheckPoint - System.currentTimeMillis());
                        if(wait <= 0){
                            LOG.error("Time Skew Detected or maybe interval is too short!");
                            ThreadUtils.sleep(2);
                            continue;
                        }
                        ThreadUtils.usleep(wait);
                        if(!stopFlag){
                            TransactionMetrics metrics = null;
                            for(VUserExecutor vuser : vusers){
                                if(metrics == null){
                                    metrics = new TransactionMetrics(vuser.getMetricsData());
                                    metrics.setSource(ConfigUtils.getDriverName(loadConfig) + "_localSoak");
                                    metrics.setTimestamp(nextCheckPoint); 
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
                    }
                }
                catch(Exception e){
                    LOG.error(e.getMessage(), e);
                }
                LOG.info("LocalSoakSlave CheckPoint Finished");
            }            
        });
    }
    
    public void stop(){
        if(!stopFlag){
            stopFlag = true;
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
            driver.shutdown();
            executor.shutdownNow();
            started = false;
        }
    }
    
    public void setGauge(GaugeSlave gauge){
        this.gauge = gauge;
    }
    
    public boolean isStarted(){
        return started;
    }
    
    public boolean driverReady(){
        return driver == null ? false : true;
    }
    
    public void setConfig(LoadConfig config){
        this.loadConfig = config;
    }

    public boolean setupDriver(){
        driver = LoadUtils.callPluginCallable(executor, loadConfig.getName(), plugins,
                new Callable<IDriver>() {
                    @Override
                    public IDriver call() {
                        return LoadUtils.createDriver(plugins, loadConfig);
                    }
                });
        return driver != null;
    }

    protected boolean startDriver(){
        try{
            if(!driverStarted){
                DriverLogger logger = LoadUtils.createLogger(SoakConfig.DrvLogger, DriverLogger.class);
                driver.setLogger(logger);
                List<ParamType> params = LoadUtils.cloneParams(ConfigUtils.getDriverParams(loadConfig));
                LoadUtils.prepareParams(params, plugins, loadConfig.getName(), null);
                driver.setParams(params);
                if(!LoadUtils.callPluginCallable(executor, loadConfig.getName(), plugins,
                        new Callable<Boolean>() {
                            @Override
                            public Boolean call() throws Exception {
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
    
    public boolean createVirtualUser(final VUserData data) {
        try {
            if(!startDriver())
                return false;

            final List<TransactionType> transTypes = ConfigUtils.getTransactions(loadConfig);
            final List<TransactionLogger> loggers = LoadUtils.createLoggers(transTypes.size());

            LoadUtils.invokePluginCallable(executor, loadConfig.getName(), plugins, new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    List<Transaction> transactions = LoadUtils.createTransactions(plugins,  loadConfig,
                            data, loggers, transTypes, driver);
                    VUserExecutor vuser = new VUserExecutor(loadConfig, loggers, transactions);
                    vusers.add(vuser);
                    LOG.info(String.format("Started Virtual User: %s", data.getUsername()));
                    vuser.run();
                    master.userFinished();
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
