package com.intel.cedar.util;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import com.intel.cedar.core.Bootstrapper;

class ServiceManager {
    private String clz;
    private Bootstrapper bootstrapper;
    private boolean shutdown = false;

    public void setStarter(String clz) {
        this.clz = clz;
    }

    public void startDaemon() {
        try {
            List<URL> urls = new ArrayList<URL>();
            urls.add(new File(SubDirectory.CONFIG.toString()).toURL());
            for (String file : new File(SubDirectory.LIBS.toString()).list()) {
                urls.add(new File(SubDirectory.LIBS.toString() + file).toURL());
            }
            ClassLoader cl = URLClassLoader.newInstance(urls
                    .toArray(new URL[] {}), URLClassLoader
                    .getSystemClassLoader());
            Thread.currentThread().setContextClassLoader(cl);
            bootstrapper = (Bootstrapper) cl.loadClass(clz).newInstance();
            bootstrapper.start();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void stopDaemon() {
        bootstrapper.stop();
    }

    private void prepare() {
        String cedar_home = System.getProperty("cedar.home");
        File logDir = new File(cedar_home + File.separator + "log");
        if (!logDir.exists())
            logDir.mkdir();
        File workDir = new File(cedar_home + File.separator + "work");
        if (!workDir.exists())
            workDir.mkdir();
        File featureDir = new File(cedar_home + File.separator + "features");
        if (!featureDir.exists())
            featureDir.mkdir();
        String cedarFile = null;
        for (String file : new File(SubDirectory.LIBS.toString()).list()) {
            if (file.startsWith("cedar-")) {
                cedarFile = SubDirectory.LIBS.toString() + file;
                System.setProperty("cedar.version", file.replace("cedar-", "")
                        .replace(".jar", ""));
                break;
            }
        }
        // copy the latest cedar to lib if there is
        for (String file : new File(SubDirectory.FEATURES.toString()).list()) {
            if (file.startsWith("cedar-")) {
                if (new File(cedarFile).delete()
                        && new File(SubDirectory.FEATURES.toString() + file)
                                .renameTo(new File(SubDirectory.LIBS.toString()
                                        + file))) {
                    cedarFile = SubDirectory.LIBS.toString() + file;
                    System.setProperty("cedar.version", file.replace("cedar-",
                            "").replace(".jar", ""));
                }
                break;
            }
        }
        System.setProperty("cedar.digest", Digester.getMD5Digest(cedarFile));
    }

    private boolean findCedarHome() {
        String cedar_home = System.getProperty("cedar.home");
        if (cedar_home == null) {
            cedar_home = System.getenv("CEDAR_HOME");
        }
        if (cedar_home == null || !new File(cedar_home).exists()) {
            return false;
        }
        System.setProperty("cedar.home", cedar_home);
        System.setProperty("jetty.home", cedar_home);
        if (clz.equals("com.intel.cedar.agent.AgentBootstrapper")
                && System.getProperty("java.cmd") == null)
            return false;
        return true;
    }

    public boolean init() {
        if (!findCedarHome()) {
            System.err.println("Cloud Test Service is not correctly installed");
            return false;
        }
        prepare();
        return true;
    }

    public void start() {
        try {
            startDaemon();
            while (!shutdown)
                Thread.sleep(5000);
        } catch (Exception e) {
        }
    }

    public void stop() {
        stopDaemon();
        shutdown = true;
    }

    /*
     * private static String getClassPath(){ String cedar_home =
     * System.getProperty("cedar.home"); StringBuilder sb = new StringBuilder();
     * sb.append(cedar_home + File.separator + "conf"); for(String file : new
     * File(cedar_home + File.separator + "lib").list()){
     * sb.append(File.pathSeparator); sb.append(cedar_home + File.separator +
     * "lib" + File.separator + file); } return sb.toString(); }
     * 
     * public void debugService(String port){ try { ProcessBuilder pb = new
     * ProcessBuilder(); List<String> command =new ArrayList<String>();
     * command.add(System.getProperty("java.cmd")); command.add("-Xms128m");
     * command.add("-Xmx512m"); command.add("-Xdebug");
     * command.add("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address="
     * +port); command.add("-cp"); command.add(getClassPath());
     * command.add("-Dcedar.home="+System.getProperty("cedar.home"));
     * command.add("-Djetty.home="+System.getProperty("jetty.home"));
     * command.add("-Djava.cmd="+System.getProperty("java.cmd"));
     * command.add(clz); pb.command(command); pb.redirectErrorStream(true);
     * pb.directory(new File(System.getProperty("cedar.home"))); process =
     * pb.start();
     * 
     * Runtime.getRuntime().addShutdownHook(new Thread() { public void run() {
     * process.destroy(); } });
     * 
     * InputStreamReader reader = new
     * InputStreamReader(process.getInputStream()); OutputStreamWriter writer =
     * new OutputStreamWriter(System.out); int n = 0; char[] buf = new
     * char[2048]; while((n = reader.read(buf)) != -1){ writer.write(buf, 0, n);
     * writer.flush(); } reader.close(); process.waitFor(); } catch (Exception
     * e) { e.printStackTrace(); } }
     * 
     * public void startApplication(String[] args){ try { ProcessBuilder pb =
     * new ProcessBuilder(); List<String> command =new ArrayList<String>();
     * command.add(System.getProperty("java.cmd")); command.add("-cp");
     * command.add(getClassPath());
     * command.add("-Dcedar.home="+System.getProperty("cedar.home"));
     * command.add("-Djetty.home="+System.getProperty("jetty.home"));
     * command.add("-Djava.cmd="+System.getProperty("java.cmd"));
     * command.add(clz); for(int i = 1; i < args.length; i++){
     * command.add(args[i]); } pb.command(command);
     * pb.redirectErrorStream(true); pb.directory(new
     * File(System.getProperty("cedar.home"))); process = pb.start();
     * 
     * Runtime.getRuntime().addShutdownHook(new Thread() { public void run() {
     * process.destroy(); } });
     * 
     * InputStreamReader reader = new
     * InputStreamReader(process.getInputStream()); OutputStreamWriter writer =
     * new OutputStreamWriter(System.out); int n = 0; char[] buf = new
     * char[2048]; while((n = reader.read(buf)) != -1){ writer.write(buf, 0, n);
     * writer.flush(); } reader.close(); process.waitFor(); } catch (Exception
     * e) { e.printStackTrace(); } }
     */
}
