package com.intel.soak.gauge.measure;

import java.util.List;
import com.intel.soak.gauge.GaugeMetrics;

public interface MetricsStore {
    public String[] getHeaders();
    public List<GaugeMetrics> getMetricsList();
    public List<Object> getSubListByColumn(String column, int max); 
    public void merge(MetricsStore nextStore) throws Exception;
    public void commitMerge();
}
