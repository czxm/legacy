package com.intel.cedar.engine.model.feature;

import com.intel.cedar.engine.model.DataModel;
import com.intel.cedar.engine.model.IDataModelDocument;

public class Trigger extends DataModel {
    private String name;
    private String launch;
    private String cron;

    public Trigger(IDataModelDocument document) {
        super(document);
        this.cron = "0 0/5 * * * ?"; // default is 5 min interval
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLaunch() {
        return launch;
    }

    public void setLaunch(String launch) {
        this.launch = launch;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }
}
