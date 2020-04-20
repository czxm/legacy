package com.intel.bigdata.master;

import com.intel.soak.gauge.GaugeMaster;
import com.intel.soak.SoakContainer;
import com.intel.soak.config.ConfigUtils;
import com.intel.soak.config.SoakConfig;
import com.intel.soak.model.LoadConfig;
import com.intel.soak.model.MergeConfig;
import com.intel.soak.utils.LoadUtils;
import com.intel.soak.utils.SoakServerUtils;
import com.intel.soak.utils.SpringBeanFactoryManager;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonController;

import java.util.ArrayList;
import java.util.List;

/**
 * This can be used to run IM agent from the console.
 *
 */
public class Main {

    private class ConsoleDaemonController implements DaemonController {

        @Override
        public void fail() throws IllegalStateException {
            signalStop();
        }

        @Override
        public void fail(Exception arg0) throws IllegalStateException {
            fail();
            print("Exception", arg0);
        }

        @Override
        public void fail(String arg0) throws IllegalStateException {
            fail();
            print(arg0);
        }

        @Override
        public void fail(String arg0, Exception arg1)
                throws IllegalStateException {
            fail();
            print(arg0, arg1);
        }

        @Override
        public void reload() throws IllegalStateException {
            try {
                master.stop();
                master.destroy();
                try {
                    master.init(daemonContext);
                    master.start();
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            } catch (Exception e1) {
                throw new IllegalStateException("Service reload failed", e1);
            }

        }

        @Override
        public void shutdown() throws IllegalStateException {
            try {
                master.stop();
                master.destroy();
                signalStop();
            } catch (Exception e1) {
                throw new IllegalStateException("Service shutdown failed", e1);
            }
        }
    }

    public static void main(String argv[]) {
        try {
            new Main().run(argv);
        } catch (Exception e) {
            System.err.println("Exception running application:");
            e.printStackTrace(System.err);
        }
    }

    private Master master = new Master();

    private DaemonContext daemonContext;

    private boolean stopRequest = false;

    public void run(String[] args) throws Exception {
        daemonContext = new ConsoleDaemonContext(new ConsoleDaemonController(),
                args);

        master.init(daemonContext);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("Running shutdown hook...");
                try {
                    System.out.println("master.stop(): " + master);
                    master.stop();
                    System.out.println("master.destroy()");
                    master.destroy();
                    System.out.println("signalStop()");
                    signalStop();
                } catch (Exception e1) {
                    throw new IllegalStateException("Service shutdown failed",
                            e1);
                }
            }
        });

        master.start();

        Thread.sleep(10000);

        SoakContainer container = SpringBeanFactoryManager.getSystemAppCxt().getBean(SoakConfig.Container, SoakContainer.class);
        List<Object> list = ConfigUtils.parseParams(args);
        List<LoadConfig> loadList = ConfigUtils.collectConfig(list, LoadConfig.class);
        for(LoadConfig c : loadList)
            container.submit(c);
        while(container.list().size() > 0){
            Thread.sleep(5);
        }

        List<MergeConfig> mergeList = new ArrayList<MergeConfig>();
        GaugeMaster gauge = SpringBeanFactoryManager.getSystemAppCxt().getBean("simpleGaugeMaster", GaugeMaster.class);
        for(LoadConfig c : loadList){
            MergeConfig config = LoadUtils.getMergeByLoad(c);
            mergeList.add(config);
        }
        SoakServerUtils.generateReport(gauge, mergeList, null);

        while (!stopRequested()) {
            try {
                waitStop();
            } catch (InterruptedException e) {
                e.printStackTrace(System.out);
            }
        }
    }

    private void print(String msg) {
        System.out.println(msg);
    }

    private void print(String msg, Throwable e) {
        System.out.println(msg);
        e.printStackTrace(System.out);
    }

    private synchronized void signalStop() {
        stopRequest = true;
        notify();
    }

    private synchronized boolean stopRequested() {
        return stopRequest;
    }

    private synchronized void waitStop() throws InterruptedException {
        wait(1000);
    }

}
