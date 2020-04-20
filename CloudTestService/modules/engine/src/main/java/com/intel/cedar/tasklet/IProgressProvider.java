package com.intel.cedar.tasklet;

public interface IProgressProvider {
    public void encounterLine(String line);

    public String getProgress();
}
