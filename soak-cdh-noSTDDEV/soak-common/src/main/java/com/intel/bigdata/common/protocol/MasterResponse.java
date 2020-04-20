package com.intel.bigdata.common.protocol;

import com.google.common.collect.ImmutableMap;

import java.io.Serializable;


public class MasterResponse implements Serializable {
    private final String sessionId;
    private final Object payload;


    public MasterResponse(String sessionId, Object payload) {
        this.sessionId = sessionId;
        this.payload = payload;
    }

    public Object getPayload() {
        return payload;
    }

    public String getSessionId() {
        return sessionId;
    }

    @Override
    public String toString() {
        return "AgentResponse{" +
                "sessionId='" + sessionId + '\'' +
                ", payload=" + payload +
                '}';
    }
}
