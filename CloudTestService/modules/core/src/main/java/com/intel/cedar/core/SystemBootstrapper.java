package com.intel.cedar.core;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.cedar.cloud.CloudBootstrapper;
import com.intel.cedar.engine.EngineBootstrapper;
import com.intel.cedar.service.WIBootstrapper;
import com.intel.cedar.storage.StorageBootstrapper;
import com.intel.cedar.util.BaseDirectory;
import com.intel.cedar.util.SubDirectory;

public class SystemBootstrapper implements Bootstrapper {
    private static Logger LOG = LoggerFactory
            .getLogger(SystemBootstrapper.class);
    private static SystemBootstrapper singleton;
    private static ThreadGroup singletonGroup;

    public static SystemBootstrapper getInstance() {
        synchronized (SystemBootstrapper.class) {
            if (singleton == null) {
                singleton = new SystemBootstrapper();
                LOG.info("Creating Bootstrapper instance.");
            } else {
                LOG.info("Returning Bootstrapper instance.");
            }
        }
        return singleton;
    }

    public static Thread makeSystemThread(Runnable r) {
        return new Thread(getThreadGroup(), r);
    }

    private static ThreadGroup getThreadGroup() {
        synchronized (SystemBootstrapper.class) {
            if (singletonGroup == null) {
                singletonGroup = new CedarThreadGroup();
            } else {
            }
        }
        return singletonGroup;
    }

    static class CedarThreadGroup extends ThreadGroup {
        CedarThreadGroup() {
            super("CloudTestService");
        }
    }

    private List<Bootstrapper> subsystems = new ArrayList<Bootstrapper>();

    @Override
    public void start() {
        LOG.info("CEDAR version: {} ({})", new Object[] {
                System.getProperty("cedar.version"),
                System.getProperty("cedar.digest") });
        for (BaseDirectory dir : BaseDirectory.values()) {
            if (!dir.check()) {
                LOG.error(dir.toString() + " doesn't exist!");
                System.exit(1);
            }
        }
        for (SubDirectory dir : SubDirectory.values()) {
            dir.create();
        }
        subsystems.add(new LocalDatabaseBootstrapper());
        subsystems.add(new WIBootstrapper());
        subsystems.add(new CloudBootstrapper());
        subsystems.add(new EngineBootstrapper());
        subsystems.add(new StorageBootstrapper());
        for (Bootstrapper b : subsystems)
            b.start();
    }

    @Override
    public void stop() {
        for (Bootstrapper b : subsystems)
            b.stop();
    }
}
