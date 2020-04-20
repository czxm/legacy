package com.intel.cedar.service.client.feature.model.ui;

import java.io.Serializable;
import java.util.List;

public interface IDependsModelProvider extends Serializable {

    /**
     * add the depend with the id
     */
    public DependModel addDepend(String dependID);

    /**
     * get the depends UI node
     */
    public List<IUINode> getDepends();

    /**
     * get depends model
     */
    public DependsModel getDependsModel();

    /**
     * get the depends values
     */
    public List<String> getDependsValues();
}
