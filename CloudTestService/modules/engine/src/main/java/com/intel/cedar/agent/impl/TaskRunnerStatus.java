package com.intel.cedar.agent.impl;

public enum TaskRunnerStatus {
    NotAvailable(0), Submitted(1), Evicted(2), Started(3), Killed(4), Timeout(5), Finished(
            6);
    TaskRunnerStatus(int status) {
        this.status = status;
    }

    public int status = 0;
}
