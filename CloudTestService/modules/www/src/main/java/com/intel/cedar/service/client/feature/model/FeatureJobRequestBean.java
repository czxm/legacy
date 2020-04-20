package com.intel.cedar.service.client.feature.model;

import java.util.List;

import com.intel.cedar.service.client.model.CedarBaseModel;

public class FeatureJobRequestBean extends CedarBaseModel {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    private String user;
    private Long userId;
    private String featureId;
    private List<Variable> variables;
    private String description;
    private Boolean reproducable;
    private List<String> receivers;

    public FeatureJobRequestBean() {

    }

    @Override
    public void refresh() {
        set("User", user);
        set("UserId", userId);
        set("FeatureId", featureId);
        set("variables", variables);
        set("Description", description);
        set("Reproducable", reproducable);
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUser() {
        return user;
    }

    public void setFeatureId(String featureId) {
        this.featureId = featureId;
    }

    public String getFeatureId() {
        return featureId;
    }

    public void setVariables(List<Variable> variables) {
        this.variables = variables;
    }

    public List<Variable> getVariables() {
        return variables;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setReproducable(boolean reproducable) {
        this.reproducable = reproducable;
    }

    public boolean isReproducable() {
        return reproducable;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public List<String> getReceivers() {
        return receivers;
    }

    public void setReceivers(List<String> receivers) {
        this.receivers = receivers;
    }

}
