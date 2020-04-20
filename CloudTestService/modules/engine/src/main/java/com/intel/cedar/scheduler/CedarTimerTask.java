package com.intel.cedar.scheduler;

import java.util.TimerTask;

public abstract class CedarTimerTask extends TimerTask {
    private String name;

    private CedarTimerTask() {
        super();
    }

    protected CedarTimerTask(String name) {
        this();
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
