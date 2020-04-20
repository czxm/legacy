package com.intel.soak.plugin;

import com.intel.soak.model.ParamType;

import java.util.List;

public interface Pluggable {
    void setParams(List<ParamType> params);
}
