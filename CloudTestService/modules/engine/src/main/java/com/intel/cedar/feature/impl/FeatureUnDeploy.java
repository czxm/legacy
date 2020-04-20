package com.intel.cedar.feature.impl;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.intel.cedar.engine.EngineFactory;
import com.intel.cedar.engine.FeatureJobInfo;
import com.intel.cedar.engine.FeaturePropsInfo;
import com.intel.cedar.feature.FeatureInfo;
import com.intel.cedar.feature.util.FeatureUtil;
import com.intel.cedar.feature.util.FileUtils;
import com.intel.cedar.tasklet.TaskletInfo;
import com.intel.cedar.util.CloudUtil;
import com.intel.cedar.util.EntityUtil;
import com.intel.cedar.util.EntityWrapper;

public class FeatureUnDeploy {
    private String featureId;
    private FeatureInfo featureInfo;

    private static Logger LOG = LoggerFactory.getLogger(FeatureUnDeploy.class);

    public FeatureUnDeploy() {
    }

    public void unDeploy() throws Exception {

    }

    public void unDeploy(String featureId, boolean check) throws Exception {
        synchronized (FeatureUnDeploy.class) {
            if (featureId == null) {
                return;
            }

            LOG.info("undeploy feature begin...");

            initUnDeploy(featureId);

            if (check && isFeatureRunning()) {
                throw new Exception("Can't undeploy an in-use feature");
            }

            removeFiles();

            removeFromDB();

            LOG.info("undeploy feature successful...");
        }
    }

    public void unDeploy(String featureId) throws Exception {
        unDeploy(featureId, true);
    }

    protected void initUnDeploy(String featureId) {
        this.featureId = featureId;
    }

    protected boolean isFeatureRunning() {
        List<String> features = Lists.newArrayList(featureId);
        for (FeatureJobInfo j : EngineFactory.getInstance().getEngine()
                .listFeatureJob(features)) {
            if (j.getStatus().isStarted())
                return true;
        }
        return false;
    }

    // delay deletion to a daemon thread due to class loader not released
    protected void removeFiles() {
        CloudUtil.asyncExec(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    System.gc();
                    Thread.sleep(10000);
                    LOG.info("clean feature files...");
                    File path = FeatureUtil.computeTargetDir(getFeatureId());
                    FileUtils.deleteFolderAndContents(path);
                } catch (Exception e) {
                }
            }
        });
    }

    protected void removeFromDB() throws Exception {
        LOG.info("clean feature from database...");
        FeatureInfo info = FeatureInfo.load(getFeatureId());
        String featureName = info.getName();
        String featureVersion = info.getVersion();
        if (info == null) {
            LOG.info("try to undeploy a non-existent feature");
            throw new Exception("try to undeploy a non-existent feature");
        }
        EntityWrapper<FeatureInfo> db = EntityUtil.getFeatureEntityWrapper();
        EntityWrapper<TaskletInfo> tdb = EntityUtil.getTaskletEntityWrapper();
        EntityWrapper<FeaturePropsInfo> fdb = EntityUtil
                .getFeaturePropsEntityWrapper();
        try {
            TaskletInfo ti = new TaskletInfo();
            for(TaskletInfo t : tdb.query(ti)){
                if(t.getFeatureId().equals(getFeatureId()))
                    tdb.delete(t);
            }
            
            FeatureInfo fei = new FeatureInfo();
            for(FeatureInfo f : db.query(fei)){
                if(f.getId().equals(getFeatureId()))
                    db.delete(f);
            }
            
            FeaturePropsInfo fi = new FeaturePropsInfo();
            fi.setFeatureName(featureName);
            fi.setFeatureVersion(featureVersion);
            List<FeaturePropsInfo> r = fdb.query(fi);
            for (FeaturePropsInfo fpi : r) {
                fdb.delete(fpi);
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        } finally {
            tdb.commit();
            db.commit();
            fdb.commit();
        }
    }

    protected FeatureInfo getFeatureInfo(String featureId) {
        if (featureInfo == null) {
            computeFeatureInfo(featureId);
        }
        return featureInfo;
    }

    protected void computeFeatureInfo(String featureId) {
        featureInfo = FeatureUtil.getFeatureInfoById(featureId);
    }

    protected String getFeatureId() {
        return featureId;
    }
}
