package com.intel.bigdata.common.protocol;

/**
 * User: kna
 */
public class CommandRequest {

    private String target;

    private Action action;

    public CommandRequest() {
        this(null, null);
    }

    public CommandRequest(String target, Action action) {
        this.target = target;
        this.action = action;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return "CommandRequest [target=" + target + ", action=" + action + "]";
    }

}

