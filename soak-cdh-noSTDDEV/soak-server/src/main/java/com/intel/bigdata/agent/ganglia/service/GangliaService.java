package com.intel.bigdata.agent.ganglia.service;

import com.intel.bigdata.common.protocol.MetricStatus;

import java.util.List;


public interface GangliaService {
    
//    String checkMetric(String host, String metric, double warning, double critical);

	List<MetricStatus> getMetricStatuses();
    
}
