package com.intel.soak.web.jetty;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.xml.XmlConfiguration;

import java.io.File;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class JettyBootstrap {

    private static Log LOG = LogFactory.getLog(JettyBootstrap.class);

    private Server jettyServer;
    private ExecutorService exec;

    abstract protected URL getJettyConfig();

    public JettyBootstrap() {
    }

    public void start() {
        try {
            File logDir = new File("log");
            FileUtils.forceMkdir(logDir);
            jettyServer = new Server();
            URL defaultConfig = getJettyConfig();
            if (defaultConfig != null) {
                XmlConfiguration jettyConfig;
                jettyConfig = new XmlConfiguration(defaultConfig);
                jettyConfig.configure(jettyServer);
            }
        } catch (Exception e) {
            LOG.error("Start jetty failed: ", e);
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

    public void stop() {
        LOG.info("Stopping Jetty service");
        try {
            if (jettyServer != null) {
                jettyServer.stop();
                jettyServer = null;
            }
        } catch (Exception e) {
        }
    }

}
