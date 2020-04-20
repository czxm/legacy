package com.intel.soak.agent.bootstrap;

import com.intel.soak.Bootable;
import com.intel.soak.utils.SpringBeanFactoryManager;

import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: joshua
 * Date: 11/1/13
 * Time: 1:30 AM
 * To change this template use File | Settings | File Templates.
 */
public class AgentSpringInitializer implements Bootable {

    private static final Set<String> CONFS = new HashSet<String>();

    static {
        CONFS.add("spring-soak-cluster-config.xml");
        CONFS.add("spring-soak-agent.xml");
        CONFS.add("spring-soak-gauge.xml");
    }

    @Override
    public synchronized void start() {
        SpringBeanFactoryManager.init(CONFS);
    }

    @Override
    public synchronized void stop() {
        SpringBeanFactoryManager.destroyAll();
    }

}
