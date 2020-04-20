package com.intel.bigdata.common.protocol;

import akka.actor.ActorRef;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.io.Serializable;

public class HeartbeatMessage implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private final ActorRef agentDispatcher;
	private final String nodeId;
    private long  timestamp;
    private final ImmutableList<MetricStatus> metrics;

    public HeartbeatMessage(String nodeId, ActorRef agentDispatcher,
            ImmutableList<MetricStatus> metrics) {
		this.agentDispatcher = agentDispatcher;
		this.nodeId = nodeId;
        this.metrics = metrics;
        this.timestamp = System.currentTimeMillis();
	}
	
	public ActorRef getAgentDispatcher() {
		return agentDispatcher;
	}

	public String getNodeId() {
		return nodeId;
	}

    public long getTimestamp(){
        return this.timestamp;
    }

    public ImmutableList<MetricStatus> getMetrics() {
        return metrics;
    }
}
