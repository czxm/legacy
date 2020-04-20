package com.intel.bigdata.agent.events;

import com.google.common.collect.ImmutableList;
import com.intel.bigdata.common.protocol.MetricStatus;

/**
 * Created with IntelliJ IDEA.
 * User: holm
 * Date: 10/25/13
 * Time: 2:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class MetricStatusList {

    public static final MetricStatusList EMPTY = new MetricStatusList(ImmutableList.<MetricStatus>of());

    private final ImmutableList<MetricStatus> statuses;

    public MetricStatusList(ImmutableList<MetricStatus> statuses) {
        this.statuses = statuses;
    }

    public ImmutableList<MetricStatus> getStatuses() {
        return statuses;
    }

}
