package com.intel.cedar.service.client.feature.model;

public class TaskletInfoBean extends ProgressInfoBean {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private String taskletId; // each tasklet should associate a taskletId,
                              // current

    // assign it with parent feature job id;

    public TaskletInfoBean() {

    }

    @Override
    public void refresh() {
        super.refresh();
    }

    public void setTaskletId(String taskletId) {
        this.taskletId = taskletId;
    }

    public String getTaskletId() {
        return taskletId;
    }
    
    public String getId(){
        return name + taskletId;
    }
}
