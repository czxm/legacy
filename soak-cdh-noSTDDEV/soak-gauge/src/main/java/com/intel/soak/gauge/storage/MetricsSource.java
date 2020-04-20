package com.intel.soak.gauge.storage;

import com.intel.soak.gauge.GaugeMetrics;
import com.intel.soak.gauge.measure.MetricsStore;

public interface MetricsSource {
    public String getName();
    public void clear() throws Exception;
    public void append(GaugeMetrics metrics) throws Exception;
    public MetricsStore load() throws Exception;
    public void save(MetricsStore store) throws Exception;
}
