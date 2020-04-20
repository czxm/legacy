package com.intel.cedar.engine.model.feature.flow;

import java.util.ArrayList;
import java.util.List;

import com.intel.cedar.engine.model.DataModel;
import com.intel.cedar.engine.model.IDataModelDocument;
import com.intel.cedar.pool.Resource;

public class TaskletsFlow extends DataModel {
    private Machine machine;
    protected List<DataModel> childs = new ArrayList<DataModel>();
    private Resource allocatedResource;

    public TaskletsFlow(IDataModelDocument document) {
        super(document);
    }

    public List<DataModel> getChilds() {
        return this.childs;
    }

    public void addChild(TaskletFlow tf) {
        if (tf == null) {
            return;
        }
        initChild(tf);
        childs.add(tf);
    }

    public void addChild(SequenceTasklets st) {
        if (st == null) {
            return;
        }
        initChild(st);
        childs.add(st);
    }

    public void addChild(ParallelTasklets pt) {
        if (pt == null) {
            return;
        }
        initChild(pt);
        childs.add(pt);
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
