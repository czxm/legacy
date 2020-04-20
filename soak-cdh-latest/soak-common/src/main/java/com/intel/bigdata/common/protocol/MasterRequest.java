package com.intel.bigdata.common.protocol;

import scala.concurrent.duration.FiniteDuration;

import java.io.Serializable;

/**
 * A request from the AgentDispatcher to the NodeManager
 * @author ovkhasch
 */
public class MasterRequest implements Serializable {
    private final String sessionId;
    private final Payload payload;

    public MasterRequest(String sessionId, Payload payload) {
        this.sessionId = sessionId;
        this.payload = payload;
    }

    public Payload getPayload() {
        return payload;
    }

    public String getSessionId() {
        return sessionId;
    }

    @Override
    public String toString() {
        return "AgentRequest{" +
                "sessionId='" + sessionId + '\'' +
                ", payload=" + payload +
                '}';
    }
}
