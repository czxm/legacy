package com.intel.soak.test;

import org.junit.Test;
import static org.junit.Assert.*;
import com.intel.soak.gauge.GaugeMetrics;
import com.intel.soak.MetricsData;

public class TestGaugeMetrics {

    @Test
    public void testCreateFromNames(){
        GaugeMetrics metrics = GaugeMetrics.fromMetricsNames(new String[]{"timestamp", "N1_SUM", "N2_AVG", "N2_NCNT", "N2_ERROR"});
        assertTrue(metrics.getMetrics().size() == 2);
        MetricsData d = metrics.getMetrics().get(0);
        assertTrue(d.getCategory().equals("N1"));
        assertTrue(d.getMetricsNames().size() == 1);
        assertTrue(d.getMetricsNames().get(0).equals("N1_SUM"));
        d = metrics.getMetrics().get(1);
        assertTrue(d.getCategory().equals("N2"));
        assertTrue(d.getMetricsNames().size() == 2);
        assertTrue(d.getMetricsNames().get(0).equals("N2_AVG"));
        assertTrue(d.getMetricsNames().get(1).equals("N2_NCNT"));
    }
}
