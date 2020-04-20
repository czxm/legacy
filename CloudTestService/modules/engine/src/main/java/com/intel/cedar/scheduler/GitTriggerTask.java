package com.intel.cedar.scheduler;

import java.io.File;
import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.cedar.engine.model.feature.GitTrigger;
import com.intel.cedar.engine.model.feature.ScmTrigger;
import com.intel.cedar.feature.FeatureInfo;
import com.intel.cedar.feature.util.GitClient;
import com.intel.cedar.service.client.feature.model.Variable;
import com.intel.cedar.util.BaseDirectory;
import com.intel.cedar.util.Utils;

public class GitTriggerTask extends ScmTriggerTask {
    private static Logger LOG = LoggerFactory.getLogger(GitTriggerTask.class);
    protected RevCommit lastCommit = null;
    protected String localrepo;

    GitClient getGitClient(GitTrigger trigger) throws Exception{
        String username = null;
        String password = null;
        String privatekey = null;
        String proxy = null;
        int port = 0;
        for(Variable v : this.getFeatureVariables()){
            if(v.getName().equals("git_username")){
                username = v.getValue();
            }
            else if(v.getName().equals("git_password")){
                password = v.getValue();
            }
            else if(v.getName().equals("git_privatekey")){
                privatekey = v.getValue();
            }
            else if(v.getName().equals("git_proxyhost")){
                proxy = v.getValue();
            }
            else if(v.getName().equals("git_proxyport")){
                port = Integer.parseInt(v.getValue());
            }            
        }
        if(trigger.getUser() != null && trigger.getUser().length() > 0)
            username = trigger.getUser();
        if(trigger.getPassword() != null && trigger.getPassword().length() > 0)
            password = trigger.getPassword();     
        if(trigger.getPrivatekey() != null && trigger.getPrivatekey().length() > 0)
            privatekey = trigger.getPrivatekey();
        if(trigger.getProxyHost() != null && trigger.getProxyHost().length() > 0)
            proxy = trigger.getProxyHost();
        if(trigger.getProxyPort() > 0)
            port = trigger.getProxyPort();        
        String url = trigger.getUrl();

        FeatureInfo fi = FeatureInfo.load(this.getFeatureId());
        String base = url.substring(url.lastIndexOf("/") + 1);
        localrepo = BaseDirectory.HOME + File.separator + fi.getContextPath() + File.separator + base + "_" + trigger.getRepoName();
        GitClient git = null;
        if(password != null){
            git = new GitClient(url, localrepo, username, password);
        }
        else if(privatekey != null){
            git = new GitClient(url, localrepo, username, Utils.decodeBase64(privatekey));
        }
        if(git != null){
            git.setProxy(proxy, port);
        }
        return git;
    }
    
    protected RevCommit getCommitByRev(GitTrigger trigger, String rev){
        GitClient git = null;
        try {
            git = getGitClient(trigger);
            if(git != null && git.openRepository()){
                return git.getCommitByName(rev);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        } finally{
            if(git != null)
                git.closeRepository();
        }
        return null;
    }
    
    protected RevCommit getLatestCommit(GitTrigger trigger) {
        GitClient git = null;
        try {
            git = getGitClient(trigger);
            if(git != null && git.openRepository()){
                git.checkout(trigger.getRepoName());
                git.update();
                return git.getHeadCommit();
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        } finally{
            if(git != null)
                git.closeRepository();
        }
        return null;
    }

    @Override
    protected void processVariables(JobDetail job, List<Variable> vars) {
        super.processVariables(job, vars);
        JobDataMap map = job.getJobDataMap();
        Object t = map.get("trigger");
        if (t instanceof GitTrigger) {
            Variable var = null;
            GitTrigger trigger = (GitTrigger) t;
            
            String privatekey = trigger.getPrivatekey();
            if(trigger.getPrivatekey_bind() != null)
                bindVariable(vars, trigger.getPrivatekey_bind(), privatekey);
            var = new Variable();
            var.setName("_git_privatekey");
            if(privatekey != null)
                var.setValue(privatekey);
            vars.add(var);
            
            String port = Integer.toString(trigger.getProxyPort());
            if (trigger.getProxyPort_bind() != null)
                bindVariable(vars, trigger.getProxyPort_bind(), port);
            var = new Variable();
            var.setName("_git_proxyport");
            if(port != null)
                var.setValue(port);
            vars.add(var);
            
            String host = trigger.getProxyHost();
            if (trigger.getProxyHost_bind() != null)
                bindVariable(vars, trigger.getProxyHost_bind(), host);
            var = new Variable();
            var.setName("_git_proxyhost");
            if(host != null)
                var.setValue(host);
            vars.add(var);
            
            var = new Variable();
            var.setName("_git_localrepo");
            var.setValue(localrepo != null ? localrepo : "");
            vars.add(var);
        }
    }

    @Override
    protected boolean foundNewCommits(ScmTrigger trigger) {
        RevCommit currentRev = null;
        if(trigger instanceof GitTrigger)
            currentRev = getLatestCommit((GitTrigger)trigger);
        if(currentRev == null)
            return false;
        String savedRevStr = this.getSavedLastRevision();
        try {
            lastCommit = getCommitByRev((GitTrigger)trigger, savedRevStr);
            RevCommit cRev = null;
            if(trigger.getRev() != null && trigger.getRev().length() > 0)
                cRev = getCommitByRev((GitTrigger)trigger, trigger.getRev());
            if(lastCommit != null && cRev != null && cRev.getCommitTime() > lastCommit.getCommitTime())
                lastCommit = cRev;
        } catch (Exception e) {
        }
        if (lastCommit == null || currentRev.getCommitTime() > lastCommit.getCommitTime()) {
            trigger.setRev(currentRev.getName());
            return true;
        }
        return false;
    }

    @Override
    protected String getLastRev() {
        return lastCommit != null ? lastCommit.getName() : null;
    }
}
