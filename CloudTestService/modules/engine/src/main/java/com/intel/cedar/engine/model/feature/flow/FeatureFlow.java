package com.intel.cedar.engine.model.feature.flow;

import com.intel.cedar.engine.model.DataModel;
import com.intel.cedar.engine.model.IDataModelDocument;
import com.intel.cedar.pool.Resource;

public class FeatureFlow extends DataModel {
    protected TaskletsFlow tasklets;
    private Machine machine;
    private Resource allocatedResource;

    public FeatureFlow(IDataModelDocument document) {
        super(document);
    }

    public void setTasklets(TaskletsFlow tasklets) {
        this.tasklets = tasklets;
        initChild(tasklets);
    }

    public TaskletsFlow getTasklets() {
        return this.tasklets;
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
    }

    public Machine getMachine() {
        return this.machine;
    }

    public void setResource(Resource resource) {
        if (this.machine != null) {
            this.allocatedResource = resource;
        }
    }

    public Resource getResource() {
        return this.allocatedResource;
    }
}
