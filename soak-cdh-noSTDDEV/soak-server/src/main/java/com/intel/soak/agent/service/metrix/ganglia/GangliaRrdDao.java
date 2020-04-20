package com.intel.soak.agent.service.metrix.ganglia;

import com.intel.soak.agent.service.metrix.dto.MetricDataDto;

/**
 * Created with IntelliJ IDEA.
 * User: joshua
 * Date: 10/24/13
 * Time: 8:19 PM
 * To change this template use File | Settings | File Templates.
 */
public interface GangliaRrdDao {

    MetricDataDto fetchMetric(final String metricName, final long from, final long to);

}
