package com.intel.cedar.tasklet.impl;

import com.intel.cedar.tasklet.IResult;
import com.intel.cedar.tasklet.ResultID;

public class Result implements IResult {

    private ResultID id = ResultID.NotAvailable;
    private String failedMessage = "";
    private String log = "";

    public Result() {
        this.id = ResultID.NotAvailable;
    }

    public Result(ResultID id) {
        this.id = id;
    }

    @Override
    public String getFailureMessage() {
        // TODO Auto-generated method stub
        return this.failedMessage;
    }

    @Override
    public ResultID getID() {
        // TODO Auto-generated method stub
        return this.id;
    }

    @Override
    public String getLog() {
        // TODO Auto-generated method stub
        return this.log;
    }

    public void setFailureMessage(String failedMessage) {
        this.failedMessage = failedMessage;
    }

    public void setID(ResultID id) {
        this.id = id;
    }

    public void setLog(String log) {
        this.log = log;
    }
}
