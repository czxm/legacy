/**
 * 
 */
package com.intel.soak;

import com.intel.soak.gauge.GaugeMetrics;
import com.intel.soak.model.MergeConfig;

/**
 * @author xzhan27
 *
 */
public interface SoakGauge {
    public void startJob(String jobName);
    public void sendMetrics(String jobName, GaugeMetrics metrics);
    public void createReport(MergeConfig config);
    public void renderChart();
    public void stopJob(String jobName);
}
