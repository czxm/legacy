package com.intel.cedar.feature;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;

import com.intel.cedar.feature.util.GitClient;
import com.intel.cedar.feature.util.SCMChangeSet;
import com.intel.cedar.util.Utils;

public abstract class GitRegressionTestFeature extends RegressionTestFeature {
	protected String gerrit_refspec;
	protected String gerrit_event = "N/A";
	protected String gerrit_change_subject = "N/A";
	protected String gerrit_change_url = "N/A";
    protected String privatekey;
    protected String localrepo;
    protected String proxy;
    protected String port;
    
    GitClient getGitClient(Environment env) throws Exception{
        GitClient git = null;
        try{
            if(password != null){
                if(localrepo == null){
                    git = new GitClient(url, env.getStorageRoot().getFolder(this.repository), username, password);
                }
                else{
                    git = new GitClient(url, localrepo, username, password);
                }
            }
            else if(privatekey != null){
                if(localrepo == null){
                    git = new GitClient(url, env.getStorageRoot().getFolder(this.repository), username, Utils.decodeBase64(privatekey));
                }
                else{
                    git = new GitClient(url, localrepo, username, Utils.decodeBase64(privatekey));
                }
            }
            if(git != null && proxy != null){
                git.setProxy(proxy, Integer.parseInt(port));
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return git;
    }
    
    @Override
    protected String getLatestModificationRevision(Environment env) throws Exception {
        GitClient git = getGitClient(env);
        try{
            if(git != null && git.openRepository()){
                git.checkout(this.repository);
                return git.getHeadCommit().getName();
            }
        }
        finally{
            if(git != null)
                git.closeRepository();
        }
        return null;
    }

    @Override
    protected List<SCMChangeSet> getModifications(Environment env) throws Exception {
        List<SCMChangeSet> result = new ArrayList<SCMChangeSet>();
        GitClient git = getGitClient(env);
        try{
            if(git != null && git.openRepository()){
                git.checkout(this.repository);
                RevCommit currR = git.getCommitByName(rev);
                RevCommit lastR = currR;
                if(lastRev != null && lastRev.length() > 0){
                    lastR = git.getCommitByName(lastRev);
                }
                for(RevCommit r : git.getCommitHistory(lastR, currR, maxModifications)){
                    result.add(0, git.getChangeSet(r));
                }
                if(result.size() > 1){
                    result.remove(0);
                }
            }
        }
        finally{
            if(git != null){
                git.closeRepository();
            }
        }
        return result;
    }

    @Override
    public void onInit(Environment env) throws Exception {
        super.onInit(env);
        try {
            if(username == null){
                username = env.getVariable("git_username").getValue();
            }
        } 
        catch (Exception e) {
        }
        try {
            if(password == null){
                password = env.getVariable("git_password").getValue();
            }
        } 
        catch (Exception e) {
        }
        try{
            if(url == null){
                url = env.getVariable("git_url").getValue();
            }
        }
        catch(Exception e){            
        }              
        try{
            if(rev == null){
                rev = env.getVariable("git_rev").getValue();
            }
        }
        catch(Exception e){            
        }
        try {
            privatekey = env.getVariable("_git_privatekey").getValue();
        } 
        catch (Exception e) {            
        }
        try{
            if(privatekey == null){
                privatekey = env.getVariable("git_privatekey").getValue();
            }
        }
        catch(Exception e){            
        }
        try{
            if(proxy == null){
                proxy = env.getVariable("_git_proxyhost").getValue();
            }
        }
        catch(Exception e){            
        }       
        try{
            if(proxy == null){
                proxy = env.getVariable("git_proxyhost").getValue();
            }
        }
        catch(Exception e){            
        }         
        try{
            if(port == null){
                port = env.getVariable("_git_proxyport").getValue();
            }
        }
        catch(Exception e){            
        }
        try{
            if(port == null){
                port = env.getVariable("git_proxyport").getValue();
            }
        }
        catch(Exception e){            
        }        
        
        try{
            if(localrepo == null){
                localrepo = env.getVariable("_git_localrepo").getValue();
            }
        }
        catch(Exception e){            
        }
        
        try{
            if(repository == null){
                repository = env.getVariable("git_branch").getValue();
            }
        }
        catch(Exception e){            
        }
        
        try{
        	gerrit_refspec = env.getVariable("gerrit_refspec").getValue();
        	gerrit_event = env.getVariable("gerrit_event").getValue();
       		gerrit_change_subject = env.getVariable("gerrit_change_subject").getValue();
       		gerrit_change_url = env.getVariable("gerrit_change_url").getValue();
        }
        catch(Exception e){        	
        }
        
        if(url == null || repository == null || username == null || 
          (password == null && privatekey == null)){
            throw new RuntimeException("Missing required Git url or authentication parameters!"); 
        }
      
        if(isCheckIn && (lastRev == null || repository == null)){
            throw new RuntimeException("Missing required regression test parameters!");
        }
        
        try{
            if(rev == null && gerrit_refspec == null){
                // initialize rev if it's not specified by submitter
                rev = this.getLatestModificationRevision(env);
                env.getVariable("git_rev").setValue(rev);
            }
        }
        catch(Exception e){            
        }
        
        if(rev == null && gerrit_refspec == null){
            throw new RuntimeException("Missing required Git revision parameters!");
        }        
        if(isNightly && lastRev == null){
            lastRev = env.getFeatureProperty("LAST_" + getTrackedRepository()
                    + "_NIGHTLY_REV", "common");
        }
    }
    
    @Override
    protected List<TaskSummaryItem> getSummaryItems(Environment env) {
        List<TaskSummaryItem> items = super.getSummaryItems(env);
        TaskSummaryItem item = new TaskSummaryItem();
        item.setName("Git Repository");
        item.setValue(getTrackedUrl());
        item.setHyperLink(getTrackedUrl().startsWith("http"));
        item.setUrl(getTrackedUrl());
        items.add(item);
        
        item = new TaskSummaryItem();
        item.setName("Branch");
        item.setValue(getTrackedRepository().substring(getTrackedRepository().lastIndexOf("/") + 1));
        items.add(item);
        
        if(getTrackedRev() == null){
        	item = new TaskSummaryItem();
        	item.setName("Code Review");
        	item.setValue(gerrit_change_url);
        	item.setHyperLink(true);
        	item.setUrl(gerrit_change_url);
        	items.add(item);
        }
        else{
        	item = new TaskSummaryItem();
        	item.setName("Revision");
        	item.setValue(getTrackedRev());
        	items.add(item);
        }
        return items;
    }
}
