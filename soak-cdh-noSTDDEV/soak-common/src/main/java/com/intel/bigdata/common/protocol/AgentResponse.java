package com.intel.bigdata.common.protocol;

import com.google.common.collect.ImmutableMap;

import java.io.Serializable;


public class AgentResponse implements Serializable {
	private final String sessionId;
	private final String nodeId;
	private final Object payload;

    // Changed process states as a result of a Application execution
    private final ImmutableMap<String, State> processStates;

    public AgentResponse(String sessionId, String nodeId, Object payload) {
        this(sessionId, nodeId, payload, null);
    }

    public AgentResponse(String sessionId, String nodeId, Object payload, ImmutableMap<String, State> processStates) {
		this.sessionId = sessionId;
		this.nodeId = nodeId;
		this.payload = payload;
        this.processStates = processStates;
    }



    public Object getPayload() {
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
        return "AgentResponse{" +
                "sessionId='" + sessionId + '\'' +
                ", nodeId='" + nodeId + '\'' +
                ", payload=" + payload +
                '}';
    }

    public ImmutableMap<String, State> getProcessStates() {
        return processStates;
    }
}
