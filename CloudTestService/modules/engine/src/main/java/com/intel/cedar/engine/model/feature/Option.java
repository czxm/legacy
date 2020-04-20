package com.intel.cedar.engine.model.feature;

import java.util.ArrayList;
import java.util.List;

import com.intel.cedar.engine.model.DataModel;
import com.intel.cedar.engine.model.IDataModelDocument;

public class Option extends DataModel {
    private boolean sendReport;
    private boolean reproducable;
    private String user;
    private String comment;
    private String receivers;
    private String failure_receivers;

    public Option(IDataModelDocument document) {
        super(document);
        this.sendReport = true;
    }

    public boolean isReproducable() {
        return reproducable;
    }

    public void setReproducable(boolean reproducable) {
        this.reproducable = reproducable;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<String> getReceivers() {
        ArrayList<String> result = new ArrayList<String>();
        if(receivers != null){
            for (String r : this.receivers.split(","))
                result.add(r);
        }
        return result;
    }

    public void setReceivers(String receivers) {
        this.receivers = receivers;
    }
    
    public List<String> getFailureReceivers() {
        ArrayList<String> result = new ArrayList<String>();
        if(failure_receivers != null){
            for (String r : this.failure_receivers.split(","))
                result.add(r);
        }
        return result;
    }

    public void setFailureReceivers(String receivers) {
        this.failure_receivers = receivers;
    }
    

    public boolean isSendReport() {
        return sendReport;
    }

    public void setSendReport(boolean sendReport) {
        this.sendReport = sendReport;
    }
}
