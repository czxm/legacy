package com.intel.cedar.service.client.feature.model;

import com.intel.cedar.service.client.model.CedarBaseModel;

public class CedarSCMLogEntry extends CedarBaseModel {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private String user;
    private String dateTime;
    private String rev;
    private String logMsg;

    public CedarSCMLogEntry() {

    }

    public CedarSCMLogEntry(String user, String dateTime, String rev,
            String logMsg) {
        this.user = user;
        this.setDateTime(dateTime);
        this.rev = rev;
        this.setLogMsg(logMsg);
    }

    @Override
    public void refresh() {
        set("User", user);
        set("DateTime", dateTime);
        set("Rev", rev);
        set("LogMsg", logMsg);
        set("Verbose", dateTime + " | " + user + " | " + rev);
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUser() {
        return user;
    }

    public void setRev(String rev) {
        this.rev = rev;
    }

    public String getRev() {
        return rev;
    }

    public void setLogMsg(String logMsg) {
        this.logMsg = logMsg;
    }

    public String getLogMsg() {
        return logMsg;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String toString() {
        return "User: " + user + "\t" + "DateTime: " + dateTime + "\t"
                + "Revision: " + rev;
    }

}
