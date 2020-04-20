package com.intel.soak.gauge.storage;

import java.util.List;

import com.intel.soak.model.ParamType;

public interface GaugeStorage {
    public void setParams(List<ParamType> params); 
    public MetricsSource openSource(String storage, String source) throws Exception;
    public boolean createStorage(String storage) throws Exception;
    public boolean deleleStorage(String storage) throws Exception;
    public String getStorageProperty(String storage, String key);
    public void setStorageProperty(String storage, String key, String value);
    public List<MetricsSource> listSource(String storage, String pattern);
}
