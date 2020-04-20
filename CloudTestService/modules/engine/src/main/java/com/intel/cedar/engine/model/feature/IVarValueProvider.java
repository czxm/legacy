package com.intel.cedar.engine.model.feature;

import java.util.List;

import com.intel.cedar.feature.impl.FeatureJar;
import com.intel.cedar.service.client.feature.model.VarValue;

public interface IVarValueProvider {
    public List<VarValue> getVarValues(FeatureJar featureJar) throws Exception;
}
