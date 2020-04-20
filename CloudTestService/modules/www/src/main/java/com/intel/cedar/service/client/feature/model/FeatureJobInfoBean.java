package com.intel.cedar.service.client.feature.model;

import com.intel.cedar.service.client.model.UserInfoBean;

public class FeatureJobInfoBean extends ProgressInfoBean {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    private String featureId;
    private String featureName;
    private String jobId; // the running Id of a feature;
    private UserInfoBean user;
    private String des;
    private String logLocation;

    public FeatureJobInfoBean() {

    }

    @Override
    public void refresh() {
        super.refresh();
        set("FeatureId", featureId);
        set("FeatureName", featureName);
        set("JobId", jobId);
        set("User", user.getUserName());
        set("Des", des);
        set("LogLocation", logLocation);
    }

    public void setFeatureId(String featureId) {
        this.featureId = featureId;
    }

    public String getFeatureId() {
        return featureId;
    }

    public void setUser(UserInfoBean user) {
        this.user = user;
    }

    public UserInfoBean getUser(){
        return user;
    }

    public void setLogLocation(String logLocation) {
        this.logLocation = logLocation;
    }

    public String getLogLocation() {
        return logLocation;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public String getDes() {
        return des;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }

    public String getFeatureName() {
        return featureName;
    }
    
    public String getId(){
        return jobId;
    }
}
