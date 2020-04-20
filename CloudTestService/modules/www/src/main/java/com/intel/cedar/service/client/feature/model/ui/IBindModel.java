package com.intel.cedar.service.client.feature.model.ui;

import java.io.Serializable;
import java.util.List;

import com.intel.cedar.service.client.feature.model.Variable;

public interface IBindModel extends Serializable {

    /**
     * get the name of bind
     */
    public String getName();

    /**
     * set the name
     */
    public void setName(String name);

    /**
     * set the feature model
     */
    public void setFeature(FeatureModel feature);

    /**
     * get the variable of this bind
     */
    public Variable getVar();

    /**
     * get the values of this bind
     */
    public List<String> getValues();
}
