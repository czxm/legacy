package com.intel.cedar.service.client.feature.model.ui;

import java.io.Serializable;
import java.util.List;

public interface IDependModel extends Serializable {
    /**
     * set the depends with ID
     */
    public void setDependID(String id);

    /**
     * get the depends ID
     */
    public String getDependID();

    /**
     * set the action with action name
     */
    public void setAction(String name);

    /**
     * set the action
     */
    public void setAction(DependsAction action);

    /**
     * get the action
     */
    public DependsAction getAction();

    /**
     * get the depends node
     */
    public IUINode getDepend();

    /**
     * get values
     */
    public List<String> getValues();
}
