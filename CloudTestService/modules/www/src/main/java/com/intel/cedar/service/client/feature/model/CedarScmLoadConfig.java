package com.intel.cedar.service.client.feature.model;

import com.extjs.gxt.ui.client.data.LoadConfig;
import com.intel.cedar.service.client.model.CedarBaseModel;

public class CedarScmLoadConfig extends CedarBaseModel implements LoadConfig {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    private String featureId;
    private Long rev;
    private Integer numOfRev;
    private String branch;
    private CedarScmType type;

    @Override
    public void refresh() {
        set("FeatureId", featureId);
        set("Rev", rev);
        set("NumOfRev", numOfRev);
        set("type", type);
        set("Branch", branch);
    }

    public CedarScmLoadConfig() {

    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getFeatureId() {
        return featureId;
    }

    public void setFeatureId(String featureId) {
        this.featureId = featureId;
    }

    public void setRev(Long rev) {
        this.rev = rev;
    }

    public Long getRev() {
        return rev;
    }

    public void setNumOfRev(Integer numOfRev) {
        this.numOfRev = numOfRev;
    }

    public Integer getNumOfRev() {
        return numOfRev;
    }

    public void setType(CedarScmType t) {
        this.type = t;
    }

    public CedarScmType getType() {
        return type;
    }

}
