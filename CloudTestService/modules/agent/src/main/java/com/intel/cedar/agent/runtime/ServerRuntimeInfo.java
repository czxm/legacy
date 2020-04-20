package com.intel.cedar.agent.runtime;

import java.util.HashMap;

import com.intel.cedar.agent.AgentConfiguration;
import com.intel.cedar.util.NetUtil;
import com.intel.xml.rss.util.rexec.RExec;

public class ServerRuntimeInfo {
    String version = System.getProperty("cedar.version");
    String digest = System.getProperty("cedar.digest");
    String server = "0.0.0.0";
    long startupTime;
    long serverTime;
    long acceptedConnections = 0;
    long executedJobs = 0;
    HashMap<String, Long> executedTasks;
    HashMap<String, Long> executedTime;

    private static ServerRuntimeInfo singleton;

    public static ServerRuntimeInfo getInstance() {
        if (singleton == null)
            singleton = new ServerRuntimeInfo();
        singleton.serverTime = System.currentTimeMillis();
        if (singleton.server.equals("0.0.0.0"))
            singleton.server = NetUtil.getHostName(AgentConfiguration
                    .getInstance().getInterface());
        return singleton;
    }

    public ServerRuntimeInfo(RExec.QueryServerResult legacy) {
        this.acceptedConnections = legacy.connectionAccepted;
        this.startupTime = legacy.startMillis;
        this.version = legacy.version;
        this.executedJobs = legacy.jobExecuted;
    }

    private ServerRuntimeInfo() {
        startupTime = System.currentTimeMillis();
        executedTasks = new HashMap<String, Long>();
        executedTime = new HashMap<String, Long>();
    }

    public synchronized void increaseConnection() {
        acceptedConnections++;
    }

    public synchronized void addExecutedTask(String tasklet) {
        Long num = executedTasks.get(tasklet);
        if (num == null) {
            num = new Long(0);
        }
        num = num + 1;
        executedTasks.put(tasklet, num);
        executedJobs++;
    }

    public synchronized void addExecutedTime(String tasklet, Long elapse) {
        Long num = executedTime.get(tasklet);
        if (num == null) {
            num = new Long(0);
        }
        num = num + elapse;
        executedTime.put(tasklet, num);
    }

    public long getDuration() {
        return serverTime - startupTime;
    }

    public long getServerTime() {
        return serverTime;
    }

    public long getStartupTime() {
        return startupTime;
    }

    public long getExecutedJobs() {
        return this.executedJobs;
    }

    public long getAcceptedConnections() {
        return acceptedConnections;
    }

    public HashMap<String, Long> getExecutedTasks() {
        return executedTasks;
    }

    public HashMap<String, Long> getExecutedTime() {
        return executedTime;
    }

    public void setAcceptedConnections(int conn) {
        this.acceptedConnections = conn;
    }

    public String getVersion() {
        return version;
    }

    public String getDigest() {
        return digest;
    }

    public String getServer() {
        return server;
    }

    public void updateVersion() {
        version = System.getProperty("cedar.version");
        digest = System.getProperty("cedar.digest");
    }
}