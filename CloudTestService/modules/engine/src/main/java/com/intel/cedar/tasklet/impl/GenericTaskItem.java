package com.intel.cedar.tasklet.impl;

import java.util.Properties;
import java.util.UUID;

import com.intel.cedar.tasklet.IResult;
import com.intel.cedar.tasklet.ITaskItem;

public class GenericTaskItem extends Properties implements ITaskItem {

    private static final long serialVersionUID = 642937828673573588L;
    protected String value = null;
    protected Result result = null;

    public GenericTaskItem() {
        this.value = "";
        this.result = new Result();
        // make unique hashCode
        this.put("_id_", UUID.randomUUID().toString());
    }

    public GenericTaskItem(String value) {
        this.value = value;
        this.result = new Result();
        // make unique hashCode
        this.put("_id_", UUID.randomUUID().toString());
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public IResult getResult() {
        // TODO Auto-generated method stub
        return this.result;
    }

    @Override
    public void setResult(IResult res) {
        // TODO Auto-generated method stub
        this.result.setID(res.getID());
        this.result.setFailureMessage(res.getFailureMessage());
        this.result.setLog(res.getLog());
    }
}
