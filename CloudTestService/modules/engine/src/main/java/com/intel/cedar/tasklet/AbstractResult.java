package com.intel.cedar.tasklet;

import com.intel.cedar.tasklet.IResult;
import com.intel.cedar.tasklet.ResultID;

public class AbstractResult implements IResult {
    private static final long serialVersionUID = -4452541695367640184L;
    
    private ResultID id = ResultID.NotAvailable;
    private String failedMessage = "";
    private String log = "";

    public AbstractResult() {
        this.id = ResultID.NotAvailable;
    }

    public AbstractResult(ResultID id) {
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
