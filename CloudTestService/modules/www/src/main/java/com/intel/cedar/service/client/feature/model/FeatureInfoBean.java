package com.intel.cedar.service.client.feature.model;

import java.io.Serializable;

import com.extjs.gxt.ui.client.data.BeanModelTag;

public class FeatureInfoBean implements BeanModelTag, Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    private String id;
    private String name;
    private String shortName;
    private String contextPath;
    private String contributor;
    private String version;
    private String description;
    private String enIcon;
    private String disIcon;
    private Boolean enabled;

    // @Override
    // public void refresh() {
    // set("Id", id);
    // set("Name", name);
    // set("ContextPath", contextPath);
    // set("Contributor", contributor);
    // set("Version", version);
    // set("Description", description);
    // set("EnIcon", enIcon);
    // set("DisIcon", disIcon);
    // set("Enabled", enabled);
    // }

    public FeatureInfoBean() {

    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setDisIcon(String disIcon) {
        this.disIcon = disIcon;
    }

    public String getDisIcon() {
        return disIcon;
    }

    public void setEnIcon(String enIcon) {
        this.enIcon = enIcon;
    }

    public String getEnIcon() {
        return enIcon;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public void setContributor(String contributor) {
        this.contributor = contributor;
    }

    public String getContributor() {
        return contributor;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getShortName() {
        return shortName;
    }

}
