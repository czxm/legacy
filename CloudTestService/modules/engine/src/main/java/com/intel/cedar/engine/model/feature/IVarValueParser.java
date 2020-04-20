package com.intel.cedar.engine.model.feature;

import java.io.Serializable;
import java.util.List;

import com.intel.cedar.service.client.feature.model.VarValue;

public interface IVarValueParser extends Serializable {
    public List<VarValue> parse(String value);
}
