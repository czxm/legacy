package com.intel.soak.agent.bootstrap;

import com.intel.soak.Bootable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: joshua
 * Date: 10/29/13
 * Time: 12:55 AM
 * To change this template use File | Settings | File Templates.
 */
public enum AgentBootstrap {

    INSTANCE;

    public static final List<Bootable> START_LIST = new LinkedList<Bootable>();
    private static final Logger LOG = LoggerFactory.getLogger(AgentBootstrap.class);

    static {
        START_LIST.add(new AgentSpringInitializer());
    }

    public static synchronized void start() {
        LOG.info("Agent Bootstrap is working...");
        for (Bootable bt : START_LIST) {
            bt.start();
        }
        LOG.info("Agent Bootstrap finished...");
    }

    public static synchronized void stop() {
        LOG.info("Stopping Soak Agent");
        for (Bootable bt : START_LIST) {
            bt.stop();
        }
        LOG.info("Soak Agent stopped...");
    }

}
