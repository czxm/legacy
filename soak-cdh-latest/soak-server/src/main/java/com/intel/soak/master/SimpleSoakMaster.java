package com.intel.soak.master;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.intel.bigdata.common.protocol.Payload;
import com.intel.soak.gauge.GaugeMaster;
import com.intel.soak.SoakMaster;
import com.intel.soak.config.ConfigUtils;
import com.intel.soak.utils.LoadUtils;
import com.intel.soak.vuser.VUserFeeder;
import com.intel.soak.model.BatchConfigType;
import com.intel.soak.model.LoadConfig;
import com.intel.soak.plugin.Plugins;
import com.intel.soak.protocol.SoakRequest;
import com.intel.soak.utils.ThreadUtils;
import com.intel.soak.vuser.VUserData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created with IntelliJ IDEA.
 * User: xzhan27
 * Date: 12/5/13
 * Time: 2:25 PM
 */
@Component("simpleSoakMaster")
@Scope("prototype")
@Lazy(true)
public class SimpleSoakMaster extends AbstractAppMaster implements SoakMaster {
    private static Logger LOG = LoggerFactory.getLogger(SimpleSoakMaster.class);

    private HashMap<String, Integer> slaveStates = new HashMap<String, Integer>();
    private String jobId;
    private LoadConfig config;
    private VUserFeeder vuserFeeder;
    private ExecutorService executor = Executors.newCachedThreadPool();
    private int interval;
    private CountDownLatch latch;
    private volatile boolean isShutdown;
    
    @Autowired
    @Qualifier("plugins")
    private Plugins plugins;

    @Autowired
    @Qualifier("simpleGaugeMaster")
    private GaugeMaster gauge;

    @Override
    public Object onReceive(Payload payload) {
        try{
            if(payload instanceof SoakRequest){
                SoakRequest request = (SoakRequest)payload;
                switch(request.getType()){
                    case VUserFinished:
                       finishVUser((Integer)request.getItem(SoakRequest.RequestKey.VUsers),
                               (String)request.getItem(SoakRequest.RequestKey.Node));
            }
            }
        }
        catch(Exception e){
            LOG.error(e.getMessage(), e);
        }
        return new Boolean(false);
    }

    private String getNextSlave(){
        String result = null;
        int min = Integer.MAX_VALUE;
        for(String node: slaveStates.keySet()){
            if(slaveStates.get(node) < min){
                result = node;
                min = slaveStates.get(node);
            }
        }
        return result;
    }

    private void incVUsers(int users, String node){
        Integer orig = slaveStates.get(node);
        slaveStates.put(node, orig + users);
    }

    private void decVUsers(int users, String node){
        Integer orig = slaveStates.get(node);
        slaveStates.put(node, orig - users);
    }

    private void finishVUser(int users, String node){
        if(latch != null){
            for(int i = 0; i < users; i++)
                latch.countDown();
        }
        decVUsers(1, node);
        LOG.info(users + " users finished on " + node);
    }

    protected boolean setupVUserFeeder() {
        vuserFeeder = LoadUtils.callPluginCallable(executor, config.getName(), plugins,
                new Callable<VUserFeeder>() {
                    @Override
                    public VUserFeeder call() {
                        return LoadUtils.createUserFeeder(plugins, config);
                    }
                });
        return vuserFeeder == null ? false : true;
    }

    public void createVirtualUsers(int startIndex, int totalUsers){
        int i = 0;
        VUserData userData = null;
        while(i < totalUsers){
            String next = getNextSlave();
            if(userData == null){
                final int userId = startIndex + i;
                userData = LoadUtils.callPluginCallable(executor, config.getName(), plugins,
                        new Callable<VUserData>() {
                            @Override
                            public VUserData call() throws Exception {
                                return vuserFeeder.feedUser(userId);
                            }
                        });
            }
            ImmutableMap<String, Object> result = sendRequest(ImmutableList.<String>of(next),
                    new SoakRequest(jobId).setType(SoakRequest.RequestType.CreateVUser)
                            .setItem(SoakRequest.RequestKey.VUserData, userData)
                            .setItem(SoakRequest.RequestKey.Node, next));
            if(checkResult(result.values().asList().get(0))){
                userData = null;
                incVUsers(1, next);
                i++;
            }
        }
    }

    boolean checkResult(Object result){
        return (result instanceof Boolean) && ((Boolean)result).booleanValue();
    }

    @Override
    public void run(LoadConfig config) {
        try{
            List<String> nodes = getLiveNodes();
            while(true){
                nodes = getLiveNodes();
                if(nodes.size() > 0)
                    break;
                else
                    Thread.sleep(2000);
                LOG.info("Waiting for Nodes Ready");
            }

            this.config = config;
            this.jobId = config.getName();
            this.interval = config.getInterval();

            LoadUtils.loadPlugins(plugins, config);
            
            if(!setupVUserFeeder())
                return;

            ImmutableMap<String, Object> result = sendRequest(ImmutableList.copyOf(nodes),
                    new SoakRequest(jobId).setType(SoakRequest.RequestType.Initialize)
                            .setItem(SoakRequest.RequestKey.Config, config)
                            .setItem(SoakRequest.RequestKey.Plugins, LoadUtils.collectPlugins(plugins, config)));
            for(String node : result.keySet()){
                if(checkResult(result.get(node)))
                    slaveStates.put(node, 0);
            }
            if(slaveStates.size() == 0){
                return;
            }


            long startTime = System.currentTimeMillis();
            int duration = ConfigUtils.getTaskDuration(config);
            int totalUsers = config.getVirtualUserConfig().getTotal();
            latch = new CountDownLatch(totalUsers);
            if(totalUsers > 0) {
                gauge.startJob(jobId);
                gauge.setJobProperty(jobId, "description", config.getDescription());
                executor.submit(new Runnable(){
                    @Override
                    public void run(){
                        while(!isShutdown){
                            ThreadUtils.sleep(interval);
                            postRequest(ImmutableList.copyOf(slaveStates.keySet()),
                                    new SoakRequest(jobId).setType(SoakRequest.RequestType.CheckPoint).setItem(SoakRequest.RequestKey.Timestamp, System.currentTimeMillis()));
                        }
                        LOG.info("SimpleSoakMaster CheckPoint Finished");
                    }
                });

                BatchConfigType batchConfig = config.getBatchConfig();
                if (batchConfig != null) {
                    int userIndex = 0;
                    int users = 0;
                    List<Integer> batchUsers = batchConfig.getBatchUsers();
                    if (batchUsers.size() == 1) {
                        int batch = batchUsers.get(0);
                        while (users < totalUsers) {
                            if (isShutdown)
                                break;
                            int startUsers = batch;
                            if (users + startUsers > totalUsers) {
                                startUsers = totalUsers - users;
                            }
                            createVirtualUsers(userIndex, startUsers);
                            users += startUsers;
                            userIndex += startUsers;
                            LOG.info(String.format("Wait %d(s) for rampup",
                                    batchConfig.getWait()));
                            ThreadUtils.sleep(batchConfig.getWait());
                        }
                    } else {
                        int startUsers = 0;
                        for (Integer batch : batchUsers) {
                            if (users < totalUsers) {
                                if (isShutdown)
                                    break;
                                startUsers = batch;
                                if (users + startUsers > totalUsers) {
                                    startUsers = totalUsers - users;
                                }
                                createVirtualUsers(userIndex, startUsers);
                                users += startUsers;
                                userIndex += startUsers;
                                LOG.info(String.format("Wait %d(s) for rampup",
                                        batchConfig.getWait()));
                                ThreadUtils.sleep(batchConfig.getWait());
                            }
                        }
                        if (users < totalUsers && !isShutdown) {
                            startUsers = totalUsers - users;
                            createVirtualUsers(userIndex, startUsers);
                            users += startUsers;
                            userIndex += startUsers;
                            LOG.info(String.format("Wait %d(s) for rampup",
                                    batchConfig.getWait()));
                            ThreadUtils.sleep(batchConfig.getWait());
                        }
                    }
                } else {
                    createVirtualUsers(0, totalUsers);
                }
            }
            LOG.info("All VUsers have been started");
            while(!isShutdown){
                try{
                    if(latch.await(5, TimeUnit.SECONDS))
                        break;
                    if(duration > 0 && (System.currentTimeMillis() - startTime) > (duration * 1000))
                        kill();
                }
                catch(Throwable t){
                    LOG.error(t.getMessage(), t);
                }
            }

            sendRequest(ImmutableList.copyOf(slaveStates.keySet()), new SoakRequest(jobId).setType(SoakRequest.RequestType.Finalize));
            gauge.stopJob(jobId);
            plugins.destroy(jobId);
            LOG.info("SimpleSoakMaster finished successfully");
        }
        catch(Exception e){
            LOG.error(e.getMessage(), e);
        }
        finally{
            isShutdown = true;
            executor.shutdownNow();
        }
    }

    @Override
    public void kill() {
        if(!isShutdown){
            postRequest(ImmutableList.copyOf(slaveStates.keySet()), new SoakRequest(jobId).setType(SoakRequest.RequestType.KillVUsers));
            isShutdown = true;
        }
    }

    public void setPlugins(Plugins plugins) {
        this.plugins = plugins;
    }
}
