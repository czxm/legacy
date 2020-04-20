package com.intel.cedar.service.client.feature.model;

import com.intel.cedar.service.client.model.CedarBaseModel;
import com.intel.cedar.service.client.model.FeatureStatusBean;
import com.intel.cedar.service.client.model.UserInfoBean;

public class HistoryInfoBean extends CedarBaseModel {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private String jobId;
    private UserInfoBean user;
    private String feature;
    private Long submitTime;
    private Long endTime;
    private FeatureStatusBean status;
    private String des;
    private String logLocation;

    public HistoryInfoBean() {

    }

    @Override
    public void refresh() {
        set("JobId", jobId);
        set("User", user.getUserName());
        set("Feature", feature);
        set("SubmitTime", submitTime);
        set("EndTime", endTime);
        set("Status", status);
        set("Desc", des);
        set("LogLocation", logLocation);
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setUser(UserInfoBean user) {
        this.user = user;
    }

    public UserInfoBean getUser() {
        return user;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public String getFeature() {
        return feature;
    }

    public void setSubmitTime(Long submitTime) {
        this.submitTime = submitTime;
    }

    public Long getSubmitTime() {
        return submitTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setStatus(FeatureStatusBean status) {
        this.status = status;
    }

    public FeatureStatusBean getStatus() {
        return status;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public String getDes() {
        return des;
    }

    public void setLogLocation(String logLocation) {
        this.logLocation = logLocation;
    }

    public String getLogLocation() {
        return logLocation;
    }

}
