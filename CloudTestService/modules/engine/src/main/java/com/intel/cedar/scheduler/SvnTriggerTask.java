package com.intel.cedar.scheduler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.cedar.engine.model.feature.ScmTrigger;
import com.intel.cedar.engine.model.feature.SvnTrigger;
import com.intel.cedar.feature.util.SCMChangeSet;
import com.intel.cedar.feature.util.SVNHistory;
import com.intel.cedar.service.client.feature.model.Variable;

public class SvnTriggerTask extends ScmTriggerTask {
    private static Logger LOG = LoggerFactory.getLogger(SvnTriggerTask.class);
    protected long lastCommit = -1;

    protected long getLatestRevision(SvnTrigger trigger) {
        String username = null;
        String password = null;
        String url = null;
        for(Variable v :this.getFeatureVariables()){
            if(v.getName().equals("svn_username")){
                username = v.getValue();
            }
            else if(v.getName().equals("svn_password")){
                password = v.getValue();
            }
        }
        SVNHistory svn = null;
        if(trigger.getUser() != null && trigger.getUser().length() > 0)
            username = trigger.getUser();
        if(trigger.getPassword() != null && trigger.getPassword().length() > 0)
            password = trigger.getPassword();
        if(trigger.getUrl() != null && trigger.getUrl().length() > 0)
            url = trigger.getUrl();
        if (url != null) {
            boolean svnProto = url.startsWith("svn");
            if (username != null && password != null)
                svn = new SVNHistory(url, username, password, svnProto);
            else
                svn = new SVNHistory(url, svnProto);
            if (svn != null) {
                try {
                    List<SCMChangeSet> entries = svn.getLatestRevisions(1, -1);
                    if (entries.size() == 1) {
                        return Long.parseLong(entries.get(0).getRev());
                    }
                } catch (Exception e) {
                }
            }
        }
        return 0;
    }

    @Override
    protected boolean foundNewCommits(ScmTrigger trigger) {
        long currentRev = -1;
        if(trigger instanceof SvnTrigger)
            currentRev = getLatestRevision((SvnTrigger)trigger);
        String savedRevStr = this.getSavedLastRevision();
        try {
            if(savedRevStr != null && savedRevStr.length() > 0){
                lastCommit = Long.parseLong(savedRevStr);
            }
            long cRev = Long.parseLong(trigger.getRev());
            if(cRev > lastCommit && lastCommit < 0)
                lastCommit = cRev;
        } catch (Exception e) {
        }
        if (currentRev > lastCommit) {
            trigger.setRev(Long.toString(currentRev));
            return true;
        }
        return false;
    }

    @Override
    protected String getLastRev() {
        if(lastCommit > 0){
            return Long.toString(lastCommit);
        }
        else{
            return null;
        }
    }
}
