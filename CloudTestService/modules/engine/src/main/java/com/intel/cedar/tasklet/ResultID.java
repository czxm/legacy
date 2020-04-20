package com.intel.cedar.tasklet;

public enum ResultID {
    Passed(0), Failed(1), Unreachable(2), Killed(3), Timeout(4), NotAvailable(5);
    ResultID(int id) {
        this.ID = id;
    }

    public boolean isSucceeded() {
        return this.ID == 0;
    }

    public boolean isUnreachable() {
        return this.ID == 2;
    }

    public int ID = 5;
}
