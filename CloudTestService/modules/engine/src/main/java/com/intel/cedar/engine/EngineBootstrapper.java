package com.intel.cedar.engine;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.cedar.core.Bootstrapper;
import com.intel.cedar.monitor.CedarMonitor;
import com.intel.cedar.pool.ResourcePool;
import com.intel.cedar.scheduler.CedarScheduler;
import com.intel.cedar.scheduler.CedarTimer;
import com.intel.cedar.scheduler.CedarTimerTask;
import com.intel.cedar.util.CedarConfiguration;

public class EngineBootstrapper implements Bootstrapper {
    private static Logger LOG = LoggerFactory
            .getLogger(EngineBootstrapper.class);
    private ExecutorService exec;

    public EngineBootstrapper() {
    }

    @Override
    public void start() {
        LOG.info("starting cedar engine");
        exec = Executors.newFixedThreadPool(1);
        exec.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    EngineFactory.getInstance().getEngine("builtin");
                    CedarMonitor.getInstance().start();
                    ResourcePool.getPool().start();
                    CedarTimer.getInstance().scheduleTask(300,
                            CedarScheduler.getInstance());
                    CedarTimer.getInstance().scheduleTask(1800,
                            new HistoryCleaner());
                    CedarTimer.getInstance().scheduleTask(60,
                            new CedarTimerTask("Refresh Configuration") {
                                @Override
                                public void run() {
                                    CedarConfiguration.getInstance().reload();
                                }
                            });
                } catch (Exception e) {
                    System.exit(1);
                }
            }
        });
    }

    @Override
    public void stop() {
        CedarMonitor.getInstance().stop();
        ResourcePool.getPool().stop();
        EngineFactory.getInstance().getEngine("builtin").shutdown();
    }
}
