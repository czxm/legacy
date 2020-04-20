package com.intel.cedar.feature.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.tmatesoft.svn.core.SVNLogEntryPath;

public class SVNChangeSet extends SCMChangeSet{
    private List<SVNLogEntryPath> changedPaths;

    public SVNChangeSet(String user, String dateTime, String rev, String logMsg) {
        this.user = user;
        this.setDateTime(dateTime);
        this.rev = rev;
        this.setLogMsg(logMsg);
    }

    public SVNChangeSet() {
    }

    @Override
    public List<SCMChangeItem> getChangeItems() {
        List<SCMChangeItem> items = new ArrayList<SCMChangeItem>();
        if(changedPaths != null){
            for(final SVNLogEntryPath e : changedPaths){
                items.add(new SCMChangeItem(){
                    @Override
                    public String getAction() {
                        return String.valueOf(e.getType());
                    }
    
                    @Override
                    public String getPath() {
                        return e.getPath();
                    }
                    
                });
            }
        }
        return items;
    }

    public void setChangeItems(Map changedPath) {
        if (changedPaths == null) {
            changedPaths = new ArrayList<SVNLogEntryPath>();
        }
        for (Object o : changedPath.values()) {
            if (o instanceof SVNLogEntryPath) {
                changedPaths.add((SVNLogEntryPath) o);
            }
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(this.getUser());
        sb.append(" | ");
        sb.append(this.getDateTime());
        sb.append("]\n");
        if (this.getLogMsg() != null) {
            sb.append(this.getLogMsg());
        }
        return sb.toString();
    }
}
