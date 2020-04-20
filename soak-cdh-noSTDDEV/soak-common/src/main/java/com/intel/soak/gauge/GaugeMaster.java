/**
 * 
 */
package com.intel.soak.gauge;

import java.util.List;

import com.intel.soak.model.LoadConfig;
import com.intel.soak.model.MergeConfig;

/**
 * @author xzhan27
 *
 */
public interface GaugeMaster {
    public void startJob(String jobName);
    public void setJobProperty(String jobName, String key, String value);
    public String getJobProperty(String jobName, String key);
    public void receiveMetrics(String jobName, GaugeMetrics[] metrics);
    public GaugeReport createReport(List<MergeConfig> configList);
    public void renderChart();
    public void stopJob(String jobName);
}
