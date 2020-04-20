package com.intel.bigdata.agent;

import com.intel.soak.agent.bootstrap.AgentBootstrap;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonController;

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
                agent.stop();
                agent.destroy();
                try {
                    agent.init(daemonContext);
                    agent.start();
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
                agent.stop();
                agent.destroy();
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

    private Agent agent = new Agent();

    private DaemonContext daemonContext;

    private boolean stopRequest = false;

    public void run(String[] args) throws Exception {
        daemonContext = new ConsoleDaemonContext(new ConsoleDaemonController(),
                args);

        agent.init(daemonContext);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("Running shutdown hook...");
                try {
                    System.out.println("agent.stop(): " + agent);
                    agent.stop();
                    System.out.println("agent.destroy()");
                    agent.destroy();
                    System.out.println("signalStop()");
                    signalStop();
                } catch (Exception e1) {
                    throw new IllegalStateException("Service shutdown failed",
                            e1);
                }
            }
        });

        agent.start();

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
