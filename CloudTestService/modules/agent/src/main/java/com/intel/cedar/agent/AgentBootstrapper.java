package com.intel.cedar.agent;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;

import com.intel.cedar.core.Bootstrapper;
import com.intel.cedar.core.JettyBootstrapper;
import com.intel.cedar.util.BaseDirectory;
import com.intel.cedar.util.SubDirectory;
import com.intel.xml.rss.util.rexec.RServer;

public class AgentBootstrapper extends JettyBootstrapper implements
        Bootstrapper {
    protected URL getJettyConfig() {
        return getClass().getClassLoader().getResource("agent-jetty.xml");
    }

    public AgentBootstrapper() {
        if (!loadJobLibrary())
            System.exit(-1);
        for (BaseDirectory dir : BaseDirectory.values()) {
            if (!dir.check()) {
                System.exit(1);
            }
        }
        for (SubDirectory dir : SubDirectory.values()) {
            dir.create();
        }

        if (AgentConfiguration.getInstance().getEnableRServer()) {
            new Thread() {
                public void run() {
                    new RServer().start();
                }
            }.start();
        }
    }

    private boolean loadJobLibrary() {
        try {
            boolean isWindows = System.getProperty("os.name").contains(
                    "Windows");
            boolean isX64 = System.getProperty("java.vm.name").contains(
                    "64-Bit");
            String libPath = System.getProperty("cedar.home") + File.separator
                    + "lib" + File.separator + (isWindows ? "win" : "linux")
                    + (isX64 ? "64" : "32");

            // This enables the java.library.path to be modified at runtime
            // From a Sun engineer at
            // http://forums.sun.com/thread.jspa?threadID=707176
            //
            Field field = ClassLoader.class.getDeclaredField("usr_paths");
            field.setAccessible(true);
            String[] paths = (String[]) field.get(null);
            String[] tmp = new String[paths.length + 1];
            System.arraycopy(paths, 0, tmp, 0, paths.length);
            tmp[paths.length] = libPath;
            field.set(null, tmp);
            System.setProperty("java.library.path", System
                    .getProperty("java.library.path")
                    + File.pathSeparator + libPath);
            System.loadLibrary("jobimpl");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
