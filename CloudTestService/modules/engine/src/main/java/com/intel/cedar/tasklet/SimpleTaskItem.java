package com.intel.cedar.tasklet;

import java.util.Properties;
import java.util.UUID;

import com.intel.cedar.tasklet.IResult;
import com.intel.cedar.tasklet.ITaskItem;

public class SimpleTaskItem extends Properties implements ITaskItem {

    private static final long serialVersionUID = 642937828673573588L;
    protected String value = null;
    protected AbstractResult result = null;

    public SimpleTaskItem() {
        this.value = "";
        this.result = new AbstractResult();
        // make unique hashCode
        this.put("_id_", UUID.randomUUID().toString());
    }

    public SimpleTaskItem(String value) {
        this.value = value;
        this.result = new AbstractResult();
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

    public int getLife() {
        try {
            return Integer.parseInt(getProperty("life", "1"));
        } catch (Exception e) {
            return 1;
        }
    }

    public void setLife(int life) {
        setProperty("life", Integer.toString(life));
    }
}
