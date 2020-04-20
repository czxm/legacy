package com.intel.bigdata.master.requests;

import com.google.common.collect.ImmutableMap;

/**
 * Created with IntelliJ IDEA.
 * User: jzhu61
 * Date: 11/1/13
 * Time: 8:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class AppRequestStatus {
    public enum RequestNodeStatus {PENDING, TIMEOUT, IN_PROGRESS, DOWN}

    private final String requestId;
    private final ImmutableMap<String, Object> node2result;

    public AppRequestStatus(
            String requestId,
            ImmutableMap<String, Object> node2result
    ) {
        this.requestId = requestId;
        this.node2result = node2result;
    }

    public ImmutableMap<String, Object> getNode2result() {
        return node2result;
    }

    public String getRequestId() {
        return requestId;
    }

    @Override
    public String toString() {
        return "AppRequestStatus{" +
                "requestId='" + requestId + '\'' +
                ", node2result='" + node2result + '\'' +
                '}';
    }

}
