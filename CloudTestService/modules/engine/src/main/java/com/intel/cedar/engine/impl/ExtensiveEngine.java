package com.intel.cedar.engine.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.intel.cedar.engine.AbstractFeatureEngine;
import com.intel.cedar.engine.FeatureJobInfo;
import com.intel.cedar.engine.FeatureJobRequest;
import com.intel.cedar.engine.FeatureStatus;
import com.intel.cedar.engine.model.feature.Feature;
import com.intel.cedar.engine.model.feature.Launch;
import com.intel.cedar.engine.model.feature.LaunchSet;
import com.intel.cedar.engine.model.feature.Option;
import com.intel.cedar.feature.FeatureInfo;
import com.intel.cedar.feature.impl.FeatureLoader;
import com.intel.cedar.scheduler.CedarTimer;
import com.intel.cedar.scheduler.CedarTimerTask;
import com.intel.cedar.service.client.feature.model.Variable;
import com.intel.cedar.storage.IFolder;
import com.intel.cedar.storage.StorageFactory;
import com.intel.cedar.user.UserInfo;
import com.intel.cedar.user.util.UserUtil;
import com.intel.cedar.util.Hashes;

public class ExtensiveEngine extends AbstractFeatureEngine {
    private static Logger LOG = LoggerFactory.getLogger(ExtensiveEngine.class);
    private List<FeatureJobInfo> jobs = Collections
            .synchronizedList(new ArrayList<FeatureJobInfo>());
    private ExecutorService exe = Executors.newCachedThreadPool();
    private List<FeatureJobInfo> pausedJobs = Collections
            .synchronizedList(new ArrayList<FeatureJobInfo>());
    private boolean paused = false;

    public ExtensiveEngine() {
        CedarTimer.getInstance().scheduleTask(30,
                new CedarTimerTask("Job Cleaner") {
                    @Override
                    public void run() {
                        synchronized (jobs) {
                            ArrayList<FeatureJobInfo> toRemove = new ArrayList<FeatureJobInfo>();
                            for (FeatureJobInfo job : jobs) {
                                if (job.getStatus().equals(
                                        FeatureStatus.Evicted)) {
                                    toRemove.add(job);
                                }
                            }
                            for (FeatureJobInfo job : toRemove) {
                                jobs.remove(job);
                            }
                        }
                    }
                });
    }

    public List<FeatureJobInfo> listFeatureJob(List<String> features) {
        if (features == null || features.size() == 0)
            return jobs;
        List<FeatureJobInfo> result = Lists.newArrayList();
        for (String f : features) {
            for (FeatureJobInfo job : jobs) {
                if (f.equals(job.getFeatureId()))
                    result.add(job);
            }
        }
        return result;
    }

    public FeatureJobInfo queryFeatureJob(String jobId) {
        for (FeatureJobInfo job : jobs) {
            if (jobId.equals(job.getId()))
                return job;
        }
        return null;
    }

    protected void submitJob(FeatureJobInfo jobInfo) {
        synchronized (jobs) {
            jobs.add(jobInfo);
        }
        if (paused) {
            synchronized (pausedJobs) {
                pausedJobs.add(jobInfo);
            }
        } else {
            jobInfo.setFuture(exe.submit(jobInfo.getRunner()));
        }
        LOG.info("Submitted new feature " + jobInfo.getFeatureId() + " :"
                + jobInfo.getId());
    }

    public String submit(FeatureJobRequest request) {
        FeatureJobInfo jobInfo = new FeatureJobInfo();
        jobInfo.setFeatureId(request.featureId);
        jobInfo.setDesc(request.description);
        jobInfo.setId(Hashes.generateId(UUID.randomUUID().toString(), "Job"));
        jobInfo.setPercent(0);
        jobInfo.setStatus(FeatureStatus.Submitted);
        jobInfo.setUserId(request.userId);
        jobInfo.setSubmitTime(System.currentTimeMillis());
        jobInfo.setReproducable(request.reproducable);
        jobInfo.setReceivers(request.receivers);
        jobInfo.setFailureReceivers(request.failure_receivers);
        if (UserUtil.getUserById(request.userId) == null
                && (request.receivers == null || request.receivers.size() == 0))
            jobInfo.setSendReport(false);
        else
            jobInfo.setSendReport(true);
        IFolder storage = StorageFactory.getInstance().getStorage().getRoot()
                .getFolder(jobInfo.getId());
        storage.create();
        jobInfo.setStorage(storage);
        List<Variable> mergedVars = null;
        try {
            Feature feature = new FeatureLoader().loadFeature(FeatureInfo
                    .load(request.featureId));
            List<Variable> featureVars = feature.getLocalVariables();
            mergedVars = mergeVariables(request.variables, featureVars);
        } catch (Exception e) {
        }
        jobInfo.setRunner(new FeatureRunner(jobInfo,
                mergedVars != null ? mergedVars : request.variables));
        submitJob(jobInfo);
        return jobInfo.getId();
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

    public List<String> submit(String featureId, String launchset) {
        Feature feature = null;
        LaunchSet ls = null;
        try {
            feature = new FeatureLoader().loadFeature(FeatureInfo
                    .load(featureId));
            ls = feature.getLaunches().getLaunchSetByName(launchset);
            return submit(featureId, feature, ls);
        } catch (Exception e) {
            LOG.info("", e);
            return new ArrayList<String>();
        }
    }

    public List<String> submit(String featureId, LaunchSet ls) {
        Feature feature = null;
        try {
            feature = new FeatureLoader().loadFeature(FeatureInfo
                    .load(featureId));
        } catch (Exception e) {
            LOG.info("", e);
            return new ArrayList<String>();
        }
        return submit(featureId, feature, ls);
    }

    protected List<String> submit(String featureId, Feature feature,
            LaunchSet ls) {
        ArrayList<String> jobIdList = new ArrayList<String>();
        if (ls != null && ls.isEnabled()) {
            Option op = ls.getOption();
            for (Launch l : ls.getModelChildren()) {
                FeatureJobInfo jobInfo = new FeatureJobInfo();
                jobInfo.setFeatureId(featureId);
                jobInfo.setDesc(op.getComment());
                jobInfo.setId(Hashes.generateId(UUID.randomUUID().toString(),
                        "Job"));
                jobInfo.setPercent(0);
                jobInfo.setStatus(FeatureStatus.Submitted);
                UserInfo user = UserUtil.loadUser(op.getUser());
                if (user != null)
                    jobInfo.setUserId(user.getId());
                else
                    jobInfo.setUserId(0L);
                jobInfo.setSubmitTime(System.currentTimeMillis());
                jobInfo.setReproducable(op.isReproducable());
                jobInfo.setReceivers(op.getReceivers());
                jobInfo.setFailureReceivers(op.getFailureReceivers());
                jobInfo.setSendReport(op.isSendReport());
                IFolder storage = StorageFactory.getInstance().getStorage()
                        .getRoot().getFolder(jobInfo.getId());
                storage.create();
                jobInfo.setStorage(storage);
                List<Variable> launchVars = l.getVariables().getVariables();
                List<Variable> featureVars = feature.getLocalVariables();
                List<Variable> mergedVars = mergeVariables(launchVars,
                        featureVars);
                jobInfo.setRunner(new FeatureRunner(jobInfo, mergedVars));
                submitJob(jobInfo);
                jobIdList.add(jobInfo.getId());
            }
        }
        return jobIdList;
    }

    public void kill(String jobId) {
        for (FeatureJobInfo job : jobs) {
            if (jobId.equals(job.getId())) {
                job.getRunner().kill();
                job.getFuture().cancel(true);
                LOG.info(job.getId() + ": killing");
                try {
                    job.getFuture().get();
                } catch (Exception e) {
                }
                return;
            }
        }
    }

    public void pause() {
        paused = true;
    }

    public void unpause() {
        synchronized (pausedJobs) {
            for (FeatureJobInfo job : pausedJobs) {
                job.setFuture(exe.submit(job.getRunner()));
            }
            pausedJobs.clear();
        }
        paused = false;
    }

    public void shutdown() {
        LOG.info("gracefully shutting down");
        exe.shutdown();
        while (!exe.isShutdown()) {
            try {
                LOG.info("waiting for engine termination");
                if (exe.awaitTermination(10, TimeUnit.SECONDS))
                    break;
            } catch (InterruptedException e) {
                break;
            }
        }

        // TODO: save all paused jobs to db
    }
}
