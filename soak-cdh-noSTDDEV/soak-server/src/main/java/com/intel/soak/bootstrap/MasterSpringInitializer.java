package com.intel.soak.bootstrap;

import com.intel.soak.Bootable;
import com.intel.soak.utils.SpringBeanFactoryManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: joshua
 * Date: 10/28/13
 * Time: 10:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class MasterSpringInitializer implements Bootable {

    private static final Set<String> CONFS = new HashSet<String>();
    private static final Log LOG = LogFactory.getLog(MasterSpringInitializer.class);

    static {
        CONFS.add("spring-soak-cluster-config.xml");
        CONFS.add("spring-soak-common.xml");
        CONFS.add("spring-soak-gauge.xml");
        CONFS.add("spring-soak-master.xml");
    }

    @Override
    public void start() {
        SpringBeanFactoryManager.init(CONFS);
    }

    @Override
    public void stop() {
        SpringBeanFactoryManager.destroyAll();
    }

}
