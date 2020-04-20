package com.intel.cedar.service.client.model;

import java.util.List;
import java.util.Properties;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.google.common.collect.Lists;
import com.google.gwt.dev.util.collect.HashMap;
import com.intel.cedar.util.protocal.ModelStream;

public class MachineInfoBean extends BeanModel {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long cloudId;
    private String imageId;
    private String imageName;
    private String os;
    private String arch;
    private String cloudName;
    private String comment;
    private List<String> features;
    private java.util.HashMap<String, String> properties;
    private Boolean managed;
    private Boolean enabled;
    private String verbose;

    public MachineInfoBean() {

    }

    public void refresh() {
        set("Id", id);
        set("CloudId", cloudId);
        set("ImageId", imageId);
        set("Os", os);
        set("Arch", arch);
        set("CloudName", cloudName);
        set("Comment", comment);
    }

    public String getVerbose() {
        verbose = os + "(" + arch + ") " + comment;
        return verbose;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setCloudId(Long cloudId) {
        this.cloudId = cloudId;
    }

    public Long getCloudId() {
        return cloudId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getImageId() {
        return imageId;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getOs() {
        return os;
    }

    public void setArch(String arch) {
        this.arch = arch;
    }

    public String getArch() {
        return arch;
    }

    public void setCloudName(String cloudName) {
        this.cloudName = cloudName;
    }

    public String getCloudName() {
        return cloudName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public List<String> getCapabilities() {
        return features;
    }

    public void setCapabilities(List<String> features) {
        this.features = features;
    }

    public java.util.HashMap<String, String> getImageProperties() {
        return properties;
    }

    public void setImageProperties(java.util.HashMap<String, String> properties) {
        this.properties = properties;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public Boolean getManaged() {
        return managed;
    }

    public void setManaged(Boolean managed) {
        this.managed = managed;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

}
