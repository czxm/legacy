package com.intel.bigdata.common.protocol;

import java.io.Serializable;
import java.util.Date;


public class MetricStatus implements Serializable {

    private final Date lastCheckTime;

    private final String name;

    private final String node;

    private final String value;

    public MetricStatus(Date lastCheckTime, String name, String node, String value) {
        this.lastCheckTime = lastCheckTime;
        this.name = name;
        this.node = node;
        this.value = value;
    }

    public Date getLastCheckTime() {
        return lastCheckTime;
    }

    /**
     * @return metric name, such as cpu_user
     */
    public String getName() {
        return name;
    }

    public String getNode() {
        return node;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "MetricStatus [lastCheckTime=" + lastCheckTime + ", name="
                + name + ", node=" + node + ", value="
                + value + "]";
    }

}
