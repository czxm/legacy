package com.intel.cedar.service.client.model;

import java.io.Serializable;

import com.extjs.gxt.ui.client.data.BaseModelData;

public class KeyPairBean extends BaseModelData implements Serializable {
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long cloudId;
    private String cloudName;
    private String keyName;
    private String keyFingerPrint;

    public KeyPairBean() {

    }

    public void refresh() {
        set("CloudName", cloudName);
        set("KeyName", keyName);
        set("KeyFingerPrint", keyFingerPrint);
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyFingerPrint(String keyFingerPrint) {
        this.keyFingerPrint = keyFingerPrint;
    }

    public String getKeyFingerPrint() {
        return keyFingerPrint;
    }

    public String toString() {
        return "KeyPairBean [ Name=" + this.keyName + ", fingerprint="
                + this.keyFingerPrint + "]";
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setCloudName(String cloudName) {
        this.cloudName = cloudName;
    }

    public String getCloudName() {
        return cloudName;
    }

    public void setCloudId(Long cloudId) {
        this.cloudId = cloudId;
    }

    public Long getCloudId() {
        return cloudId;
    }

}
