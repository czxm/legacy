package com.intel.cedar.tasklet;

public interface ITaskItem {
    public String getValue();

    public void setResult(IResult res);

    public IResult getResult();
}
