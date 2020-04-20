package com.intel.cedar.util;

public class CedarApplication {
    private static ServiceManager service = new ServiceManager();

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: start agent|service");
            System.exit(-1);
        }
        
        Runtime.getRuntime().addShutdownHook(new Thread(){
           @Override
           public void run(){
               service.stopDaemon();
           }
        });

        if (args[0].equals("agent")) {
            service.setStarter("com.intel.cedar.agent.AgentBootstrapper");
            if (service.init())
                service.startDaemon();
        } else if (args[0].equals("service")) {
            service.setStarter("com.intel.cedar.core.SystemBootstrapper");
            if (service.init())
                service.startDaemon();
        }
    }

    public static void serviceMain(String[] args) {
        String clz = null;
        if (args[0].equals("service"))
            clz = "com.intel.cedar.core.SystemBootstrapper";
        else if (args[0].equals("agent"))
            clz = "com.intel.cedar.agent.AgentBootstrapper";
        if (args[1].equals("start")) {
            service.setStarter(clz);
            if (service.init())
                service.start();
        } else if (args[1].equals("stop")) {
            service.stop();
        }
    }

    public void init(String[] args) {
        String clz = null;
        if (args[0].equals("service"))
            clz = "com.intel.cedar.core.SystemBootstrapper";
        else if (args[0].equals("agent"))
            clz = "com.intel.cedar.agent.AgentBootstrapper";
        service.setStarter(clz);
    }

    public void start() {
        if (service.init())
            service.start();
    }

    public void stop() {
        service.stop();
    }

    public void destroy() {
    }
}
