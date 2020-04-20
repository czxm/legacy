package com.intel.bigdata.master.requests;

/**
 * Created with IntelliJ IDEA.
 * User: jzhu61
 * Date: 11/1/13
 * Time: 10:16 AM
 * To change this template use File | Settings | File Templates.
 */
public class AppRequestStatusQuery {
    private final String requestId;

    public AppRequestStatusQuery(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestId() {
        return requestId;
    }
}
