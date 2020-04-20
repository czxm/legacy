package com.intel.cedar.agent.impl;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.UUID;

import com.intel.cedar.agent.IAgent;
import com.intel.cedar.tasklet.ITaskRunner;
import com.intel.cedar.util.Hashes;

public abstract class AbstractAgent implements IAgent {
    protected String host = null;
    protected String port = null;
    protected HashMap<ITaskRunner, String> runningIdMap = new HashMap<ITaskRunner, String>();
    protected HashMap<ITaskRunner, OutputStream> outputMap = new HashMap<ITaskRunner, OutputStream>();
    protected String agentId = null;

    public AbstractAgent(String host, String port) {
        this.host = host;
        this.port = port;
    }

    public boolean testConnection() {
        try {
            // don't use ICMP, as it maybe prohibited by firewall
            /*
             * InetAddress inet = InetAddress.getByName(host);
             * if(inet.isReachable(5000)){ return getServerInfo() != null; }
             * else{ return false; }
             */
            return getServerInfo() != null;

        } catch (Exception e) {
            return false;
        }
    }

    public synchronized String getAgentID() {
        if (agentId == null) {
            agentId = Hashes.generateId(UUID.randomUUID().toString(), "A");
        }
        return agentId;
    }

    public String getRunningId(ITaskRunner runner) {
        return runningIdMap.get(runner);
    }

    protected void setRunningId(ITaskRunner runner, String runningId) {
        runningIdMap.put(runner, runningId);
    }

    public void setOutputStream(ITaskRunner runner, OutputStream output) {
        this.outputMap.put(runner, output);
    }

    protected OutputStream getOutputStream(ITaskRunner runner) {
        return this.outputMap.get(runner);
    }
}
