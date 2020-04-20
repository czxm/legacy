package com.intel.cedar.service.client.feature.model.ui;

import com.intel.cedar.service.client.feature.model.uitl.UIUtils;
import com.intel.cedar.service.client.feature.view.UIBuilder;

public class SVNModel extends UIBaseNodes {

    private int max;
    
    private static final long serialVersionUID = UIUtils
            .getSerialVUID("SVNModel");

    public SVNModel() {
        super();
    }

    public void accept(UIBuilder builder) {
        if (builder == null) {
            return;
        }
        builder.visitSVN(this);
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }
    
    public URLModel getURLModel(){
        for(UIBaseNode n : this.getChildren()){
            if(n instanceof URLModel)
                return (URLModel)n;
        }
        return null;
    }
    
    public RevModel getRevModel(){
        for(UIBaseNode n : this.getChildren()){
            if(n instanceof RevModel)
                return (RevModel)n;
        }
        return null;
        
    }
    
    public LogModel getLogModel(){
        for(UIBaseNode n : this.getChildren()){
            if(n instanceof LogModel)
                return (LogModel)n;
        }
        return null;
        
    }
}
