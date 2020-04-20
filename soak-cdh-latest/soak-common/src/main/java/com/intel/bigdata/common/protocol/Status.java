package com.intel.bigdata.common.protocol;

/**
 * System status, ordered by it's criticality. Most critical statuses should
 * always be superseding less critical while shown.
 * 
 * @author andrey
 * 
 */
public enum Status {
    /**
     * The status is not available and it shouldn't. This status
     * doesn't mean an error. 
     * For instance, the metric availability depends on node role and it is defined 
     * in the <a href="{@docRoot}/metricDefinitions.html">metric definition table</a>.
     */
    NOT_AVAILABLE,
    
    /**
     * Healthy, good status. The data is available and value doesn't exceed WARNING level.
     */
    OK,
    
    /**
     * The information about status is unknown.
     * It should be available but status is not determined yet due some reason.
     * For instance, it is unknown because the metric is not collected yet.
     */
    UNKNOWN,
    
    /**
     * The data is available but its value has exceeded WARNING level defined in alert level configuration.
     */
    WARNING, 
    
    /**
     * The data is available but its value has exceeded CRITICAL level defined in alert level configuration.
     */
    CRITICAL;
}
