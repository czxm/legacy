package com.intel.cedar.feature;

import java.util.List;

import com.intel.cedar.feature.util.SCMChangeSet;
import com.intel.cedar.feature.util.SVNHistory;

public abstract class SVNRegressionTestFeature extends RegressionTestFeature {
    
    @Override
    protected String getLatestModificationRevision(Environment env) throws Exception {
        return new SVNHistory(url, username, password, url.startsWith("svn")).getLatestModificationRevision();
    }

    protected String getLatestModificationRevision(String URL, String user, String pwd) throws Exception {
        return new SVNHistory(URL, user, pwd, URL.startsWith("svn")).getLatestModificationRevision();
    }
    
    @Override
    protected List<SCMChangeSet> getModifications(Environment env) throws Exception {
        // don't include last commit revision
        return new SVNHistory(url, username, password,
                url.startsWith("svn")).
                getRevisionLogs(((lastRev != null && lastRev.length() > 0) 
                                ? Long.parseLong(lastRev) + 1 : Long.parseLong(rev)), 
                                Long.parseLong(rev));
    }

    @Override
    public void onInit(Environment env) throws Exception {
        super.onInit(env);
        try {
            if(username == null){
                username = env.getVariable("svn_username").getValue();
            }
        } 
        catch (Exception e) {
        }
        try {
            if(password == null){
                password = env.getVariable("svn_password").getValue();
            }
        } 
        catch (Exception e) {
        }
        try{
            if(url == null){
                url = env.getVariable("svn_url").getValue();
            }
        }
        catch(Exception e){            
        }              
        try{
            if(rev == null){
                rev = env.getVariable("svn_rev").getValue();
            }
        }
        catch(Exception e){            
        }
        
        if(username == null || password == null || url == null){
            throw new RuntimeException("Missing required SVN url or authentication parameters!"); 
        }
        
        if(isCheckIn && (lastRev == null || repository == null)){
            throw new RuntimeException("Missing required regression test parameters!");
        }        
        
        try{
            if(rev == null){
                // initialize rev if it's not specified by submitter
                rev = this.getLatestModificationRevision(env);
                env.getVariable("svn_rev").setValue(rev);
            }
        }
        catch(Exception e){            
        }
        if(rev == null){
            throw new RuntimeException("Missing required SVN revision parameters!"); 
        }
    }

    @Override
    protected List<TaskSummaryItem> getSummaryItems(Environment env) {
        List<TaskSummaryItem> items = super.getSummaryItems(env);
        TaskSummaryItem item = new TaskSummaryItem();
        item.setName("SVN Url");
        item.setValue(getTrackedUrl());
        item.setHyperLink(getTrackedUrl().startsWith("http"));
        item.setUrl(getTrackedUrl());
        items.add(item);

        item = new TaskSummaryItem();
        item.setName("Revision");
        item.setValue(getTrackedRev());
        items.add(item);
        return items;
    }    
}
