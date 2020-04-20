package com.intel.cedar.scheduler;

import java.util.List;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.cedar.engine.FeaturePropsInfo;
import com.intel.cedar.engine.model.feature.Feature;
import com.intel.cedar.engine.model.feature.ScmTrigger;
import com.intel.cedar.service.client.feature.model.Variable;
import com.intel.cedar.util.EntityUtil;
import com.intel.cedar.util.EntityWrapper;

public abstract class ScmTriggerTask extends CedarScheduleTask {
    private static Logger LOG = LoggerFactory.getLogger(ScmTriggerTask.class);
    protected abstract boolean foundNewCommits(ScmTrigger trigger);
    protected abstract String getLastRev();
    protected String savedRev = null;
    protected List<Variable> featureVars;
    protected String featureId;
    
    @Override
    protected boolean canExecute(JobDetail job) {
        if(!super.canExecute(job))
            return false;
        JobDataMap map = job.getJobDataMap();
        Object t = map.get("trigger");
        long lastScheduledTime = map.getLong("last_scheduled");
        if (t instanceof ScmTrigger) {
            ScmTrigger trigger = (ScmTrigger) t;
            if(System.currentTimeMillis() - lastScheduledTime < trigger.getIntervalAsInteger() * 1000){
                return false;                
            }
            map.put("last_scheduled", System.currentTimeMillis());
            findSavedLastRevision(map);
            Object f = map.get("feature");
            if (f instanceof Feature) {
                Feature ft = (Feature)f;
                featureVars = ft.getVariables();
            }
            featureId = job.getGroup();
            return foundNewCommits(trigger);
        }
        return false;
    }
    
    protected List<Variable> getFeatureVariables(){
        return this.featureVars;
    }
    
    protected String getFeatureId(){
        return this.featureId;
    }
    
    protected String getSavedLastRevision(){
        return this.savedRev;
    }
    
    private void findSavedLastRevision(JobDataMap map){
        Object t = map.get("trigger");
        ScmTrigger trigger = null;
        if (t instanceof ScmTrigger) {
            trigger = (ScmTrigger) t;
        }
        String repo = null;
        if(trigger != null)
            repo = trigger.getRepoName();
        if (repo != null && repo.length() > 0) {
            Object f = map.get("feature");
            if (f instanceof Feature) {
                EntityWrapper<FeaturePropsInfo> fdb = EntityUtil
                        .getFeaturePropsEntityWrapper();
                try {
                    Feature ft = (Feature) f;
                    FeaturePropsInfo i = new FeaturePropsInfo();
                    i.setFeatureName(ft.getName());
                    i.setFeatureVersion("common");
                    i.setKey("LAST_" + repo + "_COMMIT_REV");
                    List<FeaturePropsInfo> r = fdb.query(i);
                    if (r.size() == 1) {
                        savedRev = r.get(0).getValue();
                    }
                }
                catch(Exception e){
                    LOG.error(e.getMessage(), e);
                }
                finally {
                    fdb.rollback();
                }
            }
        }
    }
    

    @Override
    protected void processVariables(JobDetail job, List<Variable> vars) {
        JobDataMap map = job.getJobDataMap();
        Object t = map.get("trigger");
        if (t instanceof ScmTrigger) {
            Variable var = null;
            ScmTrigger trigger = (ScmTrigger) t;
            String url = trigger.getUrl();
            if (trigger.getUrl_bind() != null)
                bindVariable(vars, trigger.getUrl_bind(), url);
            var = new Variable();
            var.setName("_scm_url");
            var.setValue(url);
            vars.add(var);
            
            String rev = trigger.getRev();
            if (trigger.getRev_bind() != null)
                bindVariable(vars, trigger.getRev_bind(), rev);
            var = new Variable();
            var.setName("_scm_rev");
            var.setValue(rev);
            vars.add(var);
            
            var = new Variable();
            var.setName("_last_scm_rev");
            var.setValue(getLastRev() != null ? getLastRev() : rev);
            vars.add(var);
            
            String username = trigger.getUser();
            if (trigger.getUser_bind() != null)
                bindVariable(vars, trigger.getUser_bind(), username);
            var = new Variable();
            var.setName("_scm_username");
            if(username != null)
                var.setValue(username);
            vars.add(var);
            
            String password = trigger.getPassword();
            if (trigger.getPassword_bind() != null)
                bindVariable(vars, trigger.getPassword_bind(), password);
            var = new Variable();
            var.setName("_scm_password");
            if(password != null)
                var.setValue(password);
            vars.add(var);
            
            String repo = trigger.getRepoName();
            if (trigger.getRepo_bind() != null)
                bindVariable(vars, trigger.getRepo_bind(), repo);
            var = new Variable();
            var.setName("_scm_repository");
            if(repo != null)
                var.setValue(repo);
            vars.add(var);
        }
    }

    protected void bindVariable(List<Variable> vars, String name, String value) {
        for (Variable var : vars) {
            if (var.getName().equals(name)) {
                var.clearValues();
                var.setValue(value);
                break;
            }
        }
    }
}
