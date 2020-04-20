package com.intel.cedar.scheduler;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import org.quartz.JobDetail;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.simpl.SimpleThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.cedar.engine.model.feature.Feature;
import com.intel.cedar.engine.model.feature.Trigger;
import com.intel.cedar.feature.FeatureInfo;
import com.intel.cedar.feature.impl.FeatureLoader;
import com.intel.cedar.feature.util.FeatureUtil;

public class CedarScheduler extends CedarTimerTask {
    private static Logger LOG = LoggerFactory.getLogger(CedarScheduler.class);
    private static CedarScheduler instanceHolder = null;

    public synchronized static CedarScheduler getInstance() {
        if (instanceHolder == null) {
            instanceHolder = new CedarScheduler();
        }
        return instanceHolder;
    }

    private HashMap<String, ArrayList<Scheduler>> schedulers;

    public CedarScheduler() {
        super("Cedar Task Scheduler");
        schedulers = new HashMap<String, ArrayList<Scheduler>>();
        for (FeatureInfo feature : FeatureUtil.listFeatures(null)) {
            scheduleFeature(feature.getId());
        }
    }

    public synchronized void stopScheduledFeature(String featureId) {
        ArrayList<Scheduler> schlist = schedulers.get(featureId);
        try {
            if (schlist != null) {
                for (Scheduler sch : schlist) {
                    sch.shutdown();
                }
            }
            schedulers.remove(featureId);
        } catch (Exception e) {
            LOG.info("", e);
        }
    }

    public synchronized void scheduleFeature(String featureId) {
        ArrayList<Scheduler> schlist = new ArrayList<Scheduler>();
        schedulers.put(featureId, schlist);
        try {
            FeatureInfo feature = FeatureInfo.load(featureId);
            Feature f = new FeatureLoader().loadFeature(feature);
            for (Trigger t : f.getTriggers().getModelChildren()) {
                schlist.add(schedule(feature.getId(), f, t));
            }
            File descriptor = FeatureUtil.getFile(featureId, feature
                    .getDescriptor());
            if (descriptor.lastModified() > feature.getLastModified()) {
                feature.setLastModified(descriptor.lastModified());
                feature.saveChanges();
            }
        } catch (Exception e) {
            LOG.info("", e);
        }
    }

    @Override
    public void run() {
        try {
            for (FeatureInfo feature : FeatureUtil.listFeatures(null)) {
                ArrayList<Scheduler> schlist = schedulers.get(feature.getId());
                if (schlist != null) {
                    File descriptor = FeatureUtil.getFile(feature.getId(),
                            feature.getDescriptor());
                    if (descriptor.lastModified() > feature.getLastModified()) {
                        // feature changed
                        feature.setLastModified(descriptor.lastModified());
                        feature.saveChanges();

                        for (Scheduler sch : schlist) {
                            sch.shutdown();
                        }
                        schlist.clear();

                        Feature f = new FeatureLoader().loadFeature(feature);
                        for (Trigger t : f.getTriggers().getModelChildren()) {
                            schlist.add(schedule(feature.getId(), f, t));
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.info("", e);
        }
    }

    private Scheduler schedule(String featureId, Feature f, Trigger t)
            throws Exception {
        Properties properties = new Properties();
        properties.put(StdSchedulerFactory.PROP_SCHED_INSTANCE_NAME, featureId
                + "_" + t.getName());
        properties.put(StdSchedulerFactory.PROP_SCHED_INSTANCE_ID, featureId
                + " " + t.getName());
        properties.put(StdSchedulerFactory.PROP_THREAD_POOL_CLASS,
                SimpleThreadPool.class.getName());
        properties.put("org.quartz.threadPool.threadCount", "2");
        SchedulerFactory schedulerFactory = new StdSchedulerFactory(properties);
        Scheduler sch = schedulerFactory.getScheduler();
        sch.start();
        JobDetail job = new JobDetail();
        job.setVolatility(true);
        String cName = t.getClass().getSimpleName();
        String clzName = "com.intel.cedar.scheduler."
                + cName.substring(0, 1).toUpperCase() + cName.substring(1)
                + "Task";
        try {
            Class<?> clz = Class.forName(clzName);
            job.setJobClass(clz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        job.getJobDataMap().put("feature", f);
        job.getJobDataMap().put("trigger", t);
        job.getJobDataMap().put("last_scheduled", System.currentTimeMillis());
        job.getJobDataMap().put("jobs", new ArrayList<String>());
        job.setName(t.getLaunch());
        job.setGroup(featureId);
        String cronString = t.getCron();
        org.quartz.CronTrigger trigger = new org.quartz.CronTrigger(job
                .getName(), job.getGroup(), cronString);
        trigger.setVolatility(job.isVolatile());
        try {
            sch.scheduleJob(job, trigger);
        } catch (ObjectAlreadyExistsException e) {
            throw new RuntimeException(e);
        }
        return sch;
    }
}
