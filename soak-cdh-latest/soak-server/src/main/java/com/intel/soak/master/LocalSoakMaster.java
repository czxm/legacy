package com.intel.soak.master;

import com.intel.soak.agent.LocalSoakSlave;
import com.intel.soak.SoakMaster;
import com.intel.soak.config.ConfigUtils;
import com.intel.soak.gauge.GaugeMaster;
import com.intel.soak.model.BatchConfigType;
import com.intel.soak.model.LoadConfig;
import com.intel.soak.plugin.Plugins;
import com.intel.soak.utils.LoadUtils;
import com.intel.soak.utils.ThreadUtils;
import com.intel.soak.vuser.VUserData;
import com.intel.soak.vuser.VUserFeeder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.concurrent.*;

public class LocalSoakMaster implements SoakMaster {
    protected static Log LOG = LogFactory.getLog(LocalSoakMaster.class);

    private LocalSoakSlave slave;
    private GaugeMaster gauge;
    private int interval;
    private VUserFeeder vuserFeeder;
    private CountDownLatch latch;
    private volatile boolean isShutdown;
    private ExecutorService executor = Executors.newCachedThreadPool();
    private long nextCheckPoint;
    private Plugins plugins;

    public void setSlave(LocalSoakSlave slave) {
        this.slave = slave;
    }

    public void setGauge(GaugeMaster gauge) {
        this.gauge = gauge;
    }

    public long getNextCheckPoint() {
        return nextCheckPoint;
    }

    public void userFinished() {
        this.latch.countDown();
    }

    protected boolean setupVUserFeeder(final LoadConfig config) {
        vuserFeeder = LoadUtils.callPluginCallable(executor, config.getName(), plugins,
                new Callable<VUserFeeder>() {
                    @Override
                    public VUserFeeder call() {
                        return LoadUtils.createUserFeeder(plugins, config);
                    }
                });
        return vuserFeeder == null ? false : true;
    }

    public boolean createVirtualUsers(final int userindex, int totalUsers, LoadConfig config) {
        if (!slave.isStarted())
            slave.start();
        if (!slave.driverReady() && !slave.setupDriver()) {
            return false;
        }
        for (int i = 0; i < totalUsers; i++) {
            final int userId = userindex + i;
            VUserData data = LoadUtils.callPluginCallable(executor, config.getName(), plugins,
                    new Callable<VUserData>() {
                        @Override
                        public VUserData call() throws Exception {
                            return vuserFeeder.feedUser(userId);
                        }
                    });
            if (!slave.createVirtualUser(data))
                return false;
        }
        return true;
    }

    @Override
    public void run(LoadConfig config) {
        long startTime = System.currentTimeMillis();
        LoadUtils.loadPlugins(plugins, config);
        int duration = ConfigUtils.getTaskDuration(config);
        isShutdown = false;
        interval = config.getInterval();
        nextCheckPoint = System.currentTimeMillis() + interval * 1000;
        executor.submit(new Runnable() {
            @Override
            public void run() {
                while (!isShutdown) {
                    ThreadUtils.sleep(interval);
                    nextCheckPoint = System.currentTimeMillis() + interval * 1000;
                }
                LOG.info("LocalSoakMaster CheckPoint Finished");
            }
        });
        int totalUsers = config.getVirtualUserConfig().getTotal();
        latch = new CountDownLatch(totalUsers);
        if (slave != null && totalUsers > 0 && setupVUserFeeder(config)) {
            slave.setMaster(this);
            slave.setConfig(config);
            gauge.startJob(config.getName());
            gauge.setJobProperty(config.getName(), "description", config.getDescription());
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
                        createVirtualUsers(userIndex, startUsers, config);
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
                            createVirtualUsers(userIndex, startUsers, config);
                            users += startUsers;
                            userIndex += startUsers;
                            LOG.info(String.format("Wait %d(s) for rampup",
                                    batchConfig.getWait()));
                            ThreadUtils.sleep(batchConfig.getWait());
                        }
                    }
                    if (users < totalUsers && !isShutdown) {
                        startUsers = totalUsers - users;
                        createVirtualUsers(userIndex, startUsers, config);
                        users += startUsers;
                        userIndex += startUsers;
                        LOG.info(String.format("Wait %d(s) for rampup",
                                batchConfig.getWait()));
                        ThreadUtils.sleep(batchConfig.getWait());
                    }
                }
            } else {
                createVirtualUsers(0, totalUsers, config);
            }
        }
        LOG.info("All VUsers have been started");
        while (true) {
            try {
                if (latch.await(5, TimeUnit.SECONDS))
                    break;
                if (duration > 0 && (System.currentTimeMillis() - startTime) > (duration * 1000))
                    kill();
            } catch (Throwable t) {
                LOG.error(t.getMessage(), t);
            }
        }
        slave.stop();
        isShutdown = true;
        executor.shutdownNow();
        gauge.stopJob(config.getName());
        plugins.destroy(config.getName());
        LOG.info("LocalSoakMaster finished successfully");
    }

    @Override
    public void kill() {
        slave.stop();
        isShutdown = true;
    }

    public void setPlugins(Plugins plugins) {
        this.plugins = plugins;
    }
}
