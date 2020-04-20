package com.intel.soak.gauge;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author xzhan27
 *
 */
public class LocalGaugeSlave implements GaugeSlave {
    private static final Log LOG = LogFactory.getLog(LocalGaugeSlave.class);
    
    private GaugeMaster master;
    
    public void setMaster(GaugeMaster master){
        this.master = master;
    }
    
    @Override
    public void sendMetrics(String jobName, GaugeMetrics[] metrics) {
        master.receiveMetrics(jobName, metrics);
    }  
}
