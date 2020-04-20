package com.intel.soak;


import com.intel.soak.gauge.GaugeMetrics;

/**
 * Created with IntelliJ IDEA.
 * User: joshua
 * Date: 10/24/13
 * Time: 8:12 PM
 * To change this template use File | Settings | File Templates.
 */
public interface SoakMetricService {
    GaugeMetrics[] fetchMetrics(final String[] metricNames, final long from, final long to);
}
