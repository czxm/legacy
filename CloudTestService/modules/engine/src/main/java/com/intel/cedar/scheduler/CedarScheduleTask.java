package com.intel.cedar.scheduler;

import java.util.ArrayList;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.cedar.engine.EngineFactory;
import com.intel.cedar.engine.FeatureJobRequest;
import com.intel.cedar.engine.IEngine;
import com.intel.cedar.engine.model.feature.Feature;
import com.intel.cedar.engine.model.feature.Launch;
import com.intel.cedar.engine.model.feature.LaunchSet;
import com.intel.cedar.engine.model.feature.Option;
import com.intel.cedar.feature.FeatureInfo;
import com.intel.cedar.feature.impl.FeatureLoader;
import com.intel.cedar.service.client.feature.model.Variable;
import com.intel.cedar.user.UserInfo;
import com.intel.cedar.user.util.UserUtil;
import com.intel.cedar.util.CedarConfiguration;

public abstract class CedarScheduleTask implements Job {
    private static Logger LOG = LoggerFactory.getLogger(CedarScheduleTask.class);

    @Override
    public void execute(JobExecutionContext context)
            throws JobExecutionException {
        JobDetail job = context.getJobDetail();
        String featureId = job.getGroup();
        String launch = job.getName();
        FeatureInfo featureInfo = FeatureInfo.load(featureId);
        if (featureInfo == null)
            return;
        Feature feature = null;
        LaunchSet ls = null;
        try {
            feature = new FeatureLoader().loadFeature(featureInfo);
        } catch (Exception e) {
            LOG.info("", e);
            return;
        }
        if (feature != null) {
            ls = feature.getLaunches().getLaunchSetByName(launch);
            if (ls == null || !ls.isEnabled())
                return;
        }
        if (canExecute(job)) {
            Option op = ls.getOption();
            Object d = job.getJobDataMap().get("jobs");            
            ArrayList<String> jobs = null;
            if(d instanceof ArrayList<?>){
                jobs = (ArrayList<String>)d;
            }
            for (Launch l : ls.getModelChildren()) {
                FeatureJobRequest request = new FeatureJobRequest();
                request.featureId = featureId;
                request.description = op.getComment();
                UserInfo user = UserUtil.loadUser(op.getUser());
                request.userId = 0L;
                if (user != null)
                    request.userId = user.getId();
                request.reproducable = op.isReproducable();
                request.receivers = op.getReceivers();
                request.failure_receivers = op.getFailureReceivers();
                List<Variable> launchVars = l.getVariables().getVariables();
                List<Variable> featureVars = feature.getLocalVariables();
                List<Variable> mergedVars = mergeVariables(launchVars,
                        featureVars);
                processVariables(job, mergedVars);
                request.variables = mergedVars;
                String jobId = EngineFactory.getInstance().getEngine().submit(request);
                if(jobs != null)
                    jobs.add(jobId);
            }
        }
    }

    protected List<Variable> mergeVariables(List<Variable> launchVars,
            List<Variable> featureVars) {
        ArrayList<Variable> merged = new ArrayList<Variable>();
        for (Variable v : featureVars) {
            merged.add(v.clone());
        }
        for (Variable lv : launchVars) {
            Variable found = null;
            for (Variable mv : merged) {
                if (mv.getName().equals(lv.getName())) {
                    found = mv;
                    break;
                }
            }
            if (found != null) {
                found.clearValues();
                found.addVarValues(lv.getVarValues());
            } else {
                merged.add(lv);
            }
        }
        return merged;
    }

    protected boolean canExecute(JobDetail job){
        if(!CedarConfiguration.getInstance().getEnableTaskService())
            return false;
        Object d = job.getJobDataMap().get("jobs");            
        ArrayList<String> jobs = null;
        if(d instanceof ArrayList<?>){
            jobs = (ArrayList<String>)d;
        }
        List<String> toRemove = new ArrayList<String>();
        IEngine engine = EngineFactory.getInstance().getEngine();
        if(jobs != null){
            for(String id : jobs){
                if(engine.queryFeatureJob(id) == null){
                    toRemove.add(id);
                }
            }
            for(String i : toRemove){
                jobs.remove(i);
            }
            if(jobs.size() > 0){
                return false;
            }
        }
        return true;
    }

    protected abstract void processVariables(JobDetail job, List<Variable> vars);
}
