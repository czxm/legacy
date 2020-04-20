package com.intel.cedar.engine;

import java.util.ArrayList;
import java.util.List;

public class TaskRunnerInfo {
    private String taskName;
    private String status;
    private int progress;
    private List<AgentInfo> agents = new ArrayList<AgentInfo>();

    public class AgentInfo {
        private String agentId;
        private String host;
        private String status;
        private String progress;

        public AgentInfo(String agentId, String host, String progress,
                String status) {
            this.agentId = agentId;
            this.host = host;
            this.status = status;
            this.progress = progress;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getProgress() {
            return this.progress;
        }

        public String getAgentID() {
            return this.agentId;
        }
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public void addAgentInfo(String agentId, String host, String progress,
            String status) {
        agents.add(new AgentInfo(agentId, host, progress, status));
    }

    public void clearAgentInfo() {
        agents.clear();
    }

    public List<AgentInfo> getAgents() {
        return agents;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }
}
