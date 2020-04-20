/**
 * 
 */
package com.intel.soak.gauge;


/**
 * @author xzhan27
 *
 */
public interface GaugeSlave {
    public void sendMetrics(String jobName, GaugeMetrics[] metrics);
}
