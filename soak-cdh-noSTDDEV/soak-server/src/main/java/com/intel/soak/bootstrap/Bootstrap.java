package com.intel.soak.bootstrap;

import com.intel.soak.Bootable;
import com.intel.soak.plugin.PluginMaster;
import com.intel.soak.utils.SpringBeanFactoryManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: joshua
 * Date: 10/29/13
 * Time: 12:55 AM
 * To change this template use File | Settings | File Templates.
 */
public enum Bootstrap {

    INSTANCE;

    public static final List<Bootable> START_LIST = new LinkedList<Bootable>();
    private static final Log LOG = LogFactory.getLog(Bootstrap.class);
    private static boolean localMode = true;

    public static void setLocalMode(boolean mode){
        localMode = mode;
    }

    private static void initSpring() {
        if (localMode) {
            new LocalSpringInitializer().start();
        } else {
            new MasterSpringInitializer().start();
        }
    }

    public static synchronized void start() {
        initSpring();
        PluginMaster pluginMaster = null;
        ApplicationContext systemAppCxt = SpringBeanFactoryManager.getSystemAppCxt();
        if (localMode) {
            pluginMaster = systemAppCxt.getBean(
                    "localPluginMaster", PluginMaster.class);
        } else {
            pluginMaster = systemAppCxt.getBean(
                    "clusterPluginMaster", PluginMaster.class);
        }
        START_LIST.add(pluginMaster);

        LOG.info("Bootstrap is working...");
        for (Bootable bt : START_LIST) {
            bt.start();
        }
        LOG.info("Bootstrap finished...");
    }

    public static synchronized void stop() {
        LOG.info("Stopping Soak...");
        for (Bootable bt : START_LIST) {
            bt.stop();
        }
        LOG.info("Soak stopped...");
        if(localMode)
            System.exit(0);
    }

}
