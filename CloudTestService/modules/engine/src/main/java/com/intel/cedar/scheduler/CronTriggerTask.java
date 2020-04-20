package com.intel.cedar.scheduler;

import java.util.List;

import org.quartz.JobDetail;

import com.intel.cedar.service.client.feature.model.Variable;

public class CronTriggerTask extends CedarScheduleTask {
    @Override
    protected void processVariables(JobDetail context, List<Variable> vars) {
    }
}
