package com.intel.bigdata.common.protocol;

import scala.concurrent.duration.FiniteDuration;

import java.io.Serializable;

/**
 * A request from the NodeManager to the AgentDispatcher
 * @author ovkhasch
 */
public class AgentRequest implements Serializable {
	private final String sessionId;
    private final String nodeId;
    private final Payload payload;

    public AgentRequest(String sessionId, String nodeId, Payload payload) {
        this.sessionId = sessionId;
        this.nodeId = nodeId;
        this.payload = payload;
    }

	public Payload getPayload() {
		return payload;
	}
	
	public String getSessionId() {
		return sessionId;
	}

    public String getNodeId() {
        return nodeId;
    }

    @Override
    public String toString() {
        return "AgentRequest{" +
                "sessionId='" + sessionId + '\'' +
                ", nodeId=" + nodeId +
                ", payload=" + payload +
                '}';
    }
}
