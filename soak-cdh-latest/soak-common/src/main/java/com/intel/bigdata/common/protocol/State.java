package com.intel.bigdata.common.protocol;

import com.intel.bigdata.common.util.ProcessState;

import java.io.Serializable;

public class State implements Serializable {
    private static final long serialVersionUID = 2L;

    private final ProcessState stateValue;
    private final long timestamp;

    public State(ProcessState stateValue) {
        this.stateValue = stateValue;
        this.timestamp = System.currentTimeMillis();
    }

    public State(ProcessState stateValue, long timestamp) {
        this.stateValue = stateValue;
        this.timestamp = timestamp;
    }

    // Several factory methods for several popular states for simplicity
    public static State UNKNOWN() {
        return new State(ProcessState.UNKNOWN);
    }

    public static State STARTED() {
        return new State(ProcessState.STARTED);
    }

    public static State STOPPED() {
        return new State(ProcessState.STOPPED);
    }

    public static State FAIL() {
        return new State(ProcessState.FAIL);
    }

    public ProcessState getStateValue() {
        return stateValue;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isNewerThan(State s) {
        if (s == null) {
            return true;
        }
        return this.timestamp > s.timestamp;
    }

    public boolean hasSameState(State s)
    {
        if (this == s) {
            return true;
        } else if (s == null) {
            return false;
        }

        return this.stateValue == s.stateValue;
    }

    @Override
    public String toString() {
        return "State{" +
                "stateValue=" + stateValue +
                ", timestamp=" + timestamp +
                '}';
    }
}
