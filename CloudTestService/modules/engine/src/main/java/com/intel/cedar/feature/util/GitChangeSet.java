package com.intel.cedar.feature.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.diff.DiffEntry;

public class GitChangeSet extends SCMChangeSet{
    private List<DiffEntry> diffs;

    public GitChangeSet(String user, String dateTime, String rev, String logMsg) {
        this.user = user;
        this.setDateTime(dateTime);
        this.rev = rev;
        this.setLogMsg(logMsg);
    }

    public GitChangeSet() {
    }

    @Override
    public List<SCMChangeItem> getChangeItems() {
        List<SCMChangeItem> items = new ArrayList<SCMChangeItem>();
        if(diffs != null){
            for(final DiffEntry d : diffs){
                items.add(new SCMChangeItem(){
        
                    @Override
                    public String getAction() {
                        return d.getChangeType().name();
                    }
        
                    @Override
                    public String getPath() {
                        return d.getNewPath();
                    }
                    
                });
            }
        }
        return items;
    }

    public void setChangeItems(List<DiffEntry> entris) {
        diffs = entris;
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
