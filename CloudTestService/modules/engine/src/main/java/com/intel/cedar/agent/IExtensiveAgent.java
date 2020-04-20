package com.intel.cedar.agent;

import com.intel.cedar.engine.impl.FeaturePropsManager;
import com.intel.cedar.engine.impl.VariableManager;
import com.intel.cedar.storage.IFolder;
import com.intel.cedar.tasklet.ITaskRunner;

public interface IExtensiveAgent extends IAgent {
    public void installFeatures(ITaskRunner runner, String[] features);

    public void setVariableManager(VariableManager variables);

    public void setPropertiesManager(FeaturePropsManager props);

    public void setStorageRoot(ITaskRunner runner, IFolder storage);

    public void addPostParam(ITaskRunner runner, String key, String value);
}
