package com.intel.cedar.service.client.feature.model.ui;

import java.util.ArrayList;
import java.util.List;

import com.intel.cedar.service.client.feature.model.Variable;
import com.intel.cedar.service.client.feature.model.uitl.UIUtils;

public class BindModel implements IBindModel {

    private FeatureModel feature;
    private String name;
    private Variable uVar;

    public static List<String> EMPTY = new ArrayList<String>();
    private static final long serialVersionUID = UIUtils
            .getSerialVUID("BindModel");

    public BindModel() {

    }

    public BindModel(String name) {
        this.name = name;
    }

    public BindModel(FeatureModel feature, String name) {
        this.feature = feature;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> getValues() {
        if (uVar == null) {
            computeVariable();
        }
        if (uVar == null) {
            return EMPTY;
        }

        return uVar.getValues();
    }

    @Override
    public Variable getVar() {
        if (uVar == null) {
            computeVariable();
        }

        return uVar;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setFeature(FeatureModel feature) {
        this.feature = feature;
    }

    protected void computeVariable() {
        if (name == null || feature == null) {
            return;
        }

        VariableBuffer varBuff = feature.getVarBuff();
        uVar = varBuff.getVariable(name);
    }
}
