package com.intel.cedar.service.client.feature.model;

import com.intel.cedar.service.client.model.CedarBaseModel;

public class TestFeature extends CedarBaseModel {

    /**
	 * 
	 */
    private static final long serialVersionUID = -3768395996355206989L;

    private String componentName;

    public TestFeature() {

    }

    public TestFeature(String compName) {
        this.setComponentName(compName);
    }

    @Override
    public void refresh() {
        set("ComponentName", componentName);
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getComponentName() {
        return componentName;
    }

}
