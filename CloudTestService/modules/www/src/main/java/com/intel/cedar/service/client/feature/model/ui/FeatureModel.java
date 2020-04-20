package com.intel.cedar.service.client.feature.model.ui;

import com.intel.cedar.service.client.feature.model.uitl.UIUtils;

public class FeatureModel extends UIBaseNodes {
    protected String name;
    protected VariableBuffer varBuff;
    private String featureID;

    private static final long serialVersionUID = UIUtils
            .getSerialVUID(UIUtils.UI_FEATURE);

    public FeatureModel() {
        super();
        varBuff = new VariableBuffer();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public VariableBuffer getVarBuff() {
        return varBuff;
    }

    /**
     * get the first window if it exist, return null if there is no window
     */
    public WindowModel getWindow() {
        UIBaseNode node = getChildren().get(0);
        if (node == null) {
            return null;
        }

        if (!(node instanceof WindowModel)) {
            return null;
        }

        return (WindowModel) node;
    }

    /**
     * get the first form of the first window return null if there is no such
     * form
     */
    public FormModel getForm() {
        WindowModel wModel = getWindow();
        if (wModel == null) {
            return null;
        }

        return wModel.getForm();
    }

    public void setFeatureID(String featureID) {
        this.featureID = featureID;
    }

    public String getFeatureID() {
        return featureID;
    }
}
