package com.intel.cedar.feature.util;

import java.util.List;

public abstract class SCMChangeSet {
    protected String user;
    protected String dateTime;
    protected String rev;
    protected String logMsg;
    
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getRev() {
        return rev;
    }

    public void setRev(String rev) {
        this.rev = rev;
    }

    public String getLogMsg() {
        return logMsg;
    }

    public void setLogMsg(String logMsg) {
        this.logMsg = logMsg;
    }

    public abstract List<SCMChangeItem> getChangeItems();

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(this.getUser());
        sb.append(" | ");
        sb.append(this.getDateTime());
        sb.append("]\n");
        if (this.getLogMsg() != null) {
            sb.append(this.getLogMsg());
        }
        return sb.toString();
    }
}
