package com.intel.soak.agent.service.metrix;

import com.intel.soak.agent.service.metrix.dto.MetricDataDto;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: joshua
 * Date: 10/24/13
 * Time: 8:12 PM
 * To change this template use File | Settings | File Templates.
 */
public interface SoakMetricService {

    MetricDataDto fetchMetric(final String metricName, final long from, final long to);
    MetricDataDto fetchMetric(final String metricName, final Date from, final Date to);

}
