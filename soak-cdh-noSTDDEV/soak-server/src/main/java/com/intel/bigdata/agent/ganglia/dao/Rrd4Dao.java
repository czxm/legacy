package com.intel.bigdata.agent.ganglia.dao;

import com.intel.bigdata.common.protocol.MetricStatus;

import java.util.List;

public interface Rrd4Dao {
	void saveMetricValue(MetricStatus ms);

	void saveMetrics(List<MetricStatus> metricStatuses);
}
