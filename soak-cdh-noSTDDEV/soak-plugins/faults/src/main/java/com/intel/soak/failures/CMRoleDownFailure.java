package com.intel.soak.failures;

import com.intel.soak.util.CMClient;
import com.intel.soak.util.CMClientFactory;
import org.apache.bigtop.itest.failures.AbstractFailure;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

public class CMRoleDownFailure extends AbstractFailure {
    private static Log LOG = LogFactory.getLog(CMRoleDownFailure.class);

    private static final int SLEEP_TIME = 100;
    private String service;
    private String role;
    private CMClient client = CMClientFactory.INSTANCE.createClient();
    private boolean waiting = false;
    /**
     * Simple constructor for failures, uses default values.
     * @param hosts list of hosts this failure will be executed on.
     */
    public CMRoleDownFailure(List<String> hosts, String service, String role) {
        super(hosts);
        this.service = service;
        this.role = role;
    }

    /**
     * Constructor allowing to set all params.
     *
     * @param hosts list of hosts the failure will be running against
     * @param startDelay how long (in millisecs) failure will wait before starting
     */
    public CMRoleDownFailure(List<String> hosts, String service, String role, long startDelay) {
        super(hosts, startDelay);
        this.service = service;
        this.role = role;
    }

    @Override
    public void run() {
        try {
            if (startDelay > 0) {
                try {
                    Thread.sleep(startDelay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            runFailCommands();

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    waiting = true;
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException e) {
                    return;
                }
            }
        } finally {
            waiting = false;
            runRestoreCommands();
        }
    }

    public boolean isWaiting(){
        return waiting;
    }

    private void runRestoreCommands() {
        for(String host : hosts){
            String theRole = client.findRoleByHost(host, service, role);
            do{
                client.startProcess(theRole);
                LOG.info("Starting " + theRole);
            }
            while(!client.waitForProcess(theRole, true, 60));
            LOG.info(theRole +" is started");
        }
    }

    private void runFailCommands() {
        for(String host : hosts){
            String theRole = client.findRoleByHost(host, service, role);
            do{
                client.stopProcess(theRole);
                LOG.info("Stopping " + theRole);
            }
            while(!client.waitForProcess(theRole, false, 60));
            LOG.info(theRole + " is stopped");
        }
    }

}

