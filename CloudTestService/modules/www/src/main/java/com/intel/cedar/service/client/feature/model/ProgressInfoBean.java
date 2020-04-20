package com.intel.cedar.service.client.feature.model;

import com.intel.cedar.service.client.model.CedarBaseModel;

public abstract class ProgressInfoBean extends CedarBaseModel {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    protected String name;
    protected Boolean isStarted;
    protected Long startTime;
    protected Long endTime;
    protected Double progress;

    public ProgressInfoBean() {

    }

    @Override
    public void refresh() {
        set("Id", getId());
        set("Name", name);
        set("IsStarted", isStarted);
        set("StartTime", startTime);
        set("EndTime", endTime);
        set("Progress", progress);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setProgress(Double progress) {
        this.progress = progress;
    }

    public Double getProgress() {
        return progress;
    }

    public void setIsStarted(Boolean isStarted) {
        this.isStarted = isStarted;
    }

    public Boolean getIsStarted() {
        return isStarted;
    }
    
    abstract String getId();
}
