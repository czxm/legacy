package com.intel.cedar.service.client.feature.model;

public class AgentInfoBean extends ProgressInfoBean {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    // private String host; substitute this field with name in ProgressInfoBean
    private String progressLine;
    
    public AgentInfoBean() {

    }

    @Override
    public void refresh() {
        set("Id", getId());
        set("Name", name);
        set("Progress", progressLine);
    }
    
    public void setProgressLine(String progress) {
        this.progressLine = progress;
    }

    public String getProgressLine() {
        return progressLine;
    }
    
    public String getId(){
        return this.name;
    }
}
