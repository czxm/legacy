package com.intel.bigdata.master.requests;

/**
 * Created with IntelliJ IDEA.
 * User: jzhu61
 * Date: 11/1/13
 * Time: 10:26 AM
 * To change this template use File | Settings | File Templates.
 */
public class AppRequestStatusMissing {
    private final String reqId;

    public AppRequestStatusMissing(String reqId) {
        this.reqId = reqId;
    }

    public String getReqId() {
        return reqId;
    }
}
