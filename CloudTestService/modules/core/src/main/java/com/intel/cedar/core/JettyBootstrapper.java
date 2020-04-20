package com.intel.cedar.core;

import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.mortbay.jetty.Server;
import org.mortbay.xml.XmlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class JettyBootstrapper implements Bootstrapper {
    private static Logger LOG = LoggerFactory
            .getLogger(JettyBootstrapper.class);
    private Server jettyServer;
    private ExecutorService exec;

    abstract protected URL getJettyConfig();

    public JettyBootstrapper() {
    }

    @Override
    public void start() {
        try {
            jettyServer = new org.mortbay.jetty.Server();
            URL defaultConfig = getJettyConfig();
            if (defaultConfig != null) {
                XmlConfiguration jettyConfig;
                jettyConfig = new XmlConfiguration(defaultConfig);
                jettyConfig.configure(jettyServer);
            }
        } catch (Exception e) {
            LOG.error("", e);
            jettyServer = null;
        }
        if (jettyServer != null) {
            LOG.info("Starting Jetty service");
            exec = Executors.newFixedThreadPool(1);
            exec.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        jettyServer.start();
                    } catch (Exception e) {
                        System.exit(1);
                    }
                }
            });
        }
    }

    @Override
    public void stop() {
        try {
            if (jettyServer != null) {
                jettyServer.stop();
                jettyServer = null;
            }
        } catch (Exception e) {
        }
    }
}
