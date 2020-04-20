package com.intel.cedar.engine.model.feature.flow;

import java.util.ArrayList;
import java.util.Iterator;

import com.intel.cedar.engine.impl.Evaluator;
import com.intel.cedar.engine.model.DataModel;
import com.intel.cedar.engine.model.IDataModel;
import com.intel.cedar.engine.model.IDataModelDocument;
import com.intel.cedar.pool.Resource;

public class TaskletFlow extends DataModel {

    public enum FailurePolicy {
        Exit, Ignore, Reschedule;
    }

    private String id;
    private String name;
    private Machine machine;
    private Items items;
    private String timeout;
    private String onFail;
    private boolean debug;
    private Resource allocatedResource;

    public TaskletFlow(IDataModelDocument document) {
        super(document);
        items = new Items(document);
        initChild(items);
    }

    public Iterator<IDataModel> iterate() {
        ArrayList<IDataModel> children = new ArrayList<IDataModel>(4);
        children.add(machine);
        children.add(items);
        return children.iterator();
    }

    public void setID(String id) {
        this.id = id;
    }

    public String getID() {
        return this.id;
    }

    public String getTimeout() {
        return this.timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
    }

    public Machine getMachine() {
        if (this.machine == null)
            return findEnclosedMachine();
        else
            return this.machine;
    }

    private Machine findEnclosedMachine() {
        IDataModel parent = getParent();
        while (true) {
            if (parent instanceof TaskletsFlow) {
                if (((TaskletsFlow) parent).getMachine() != null) {
                    return ((TaskletsFlow) parent).getMachine();
                } else {
                    parent = parent.getParent();
                }
            } else if (parent instanceof FeatureFlow) {
                return ((FeatureFlow) parent).getMachine();
            } else {
                return null;
            }
        }
    }

    private IDataModel findEnclosedMachineHolder() {
        IDataModel parent = getParent();
        while (true) {
            if (parent instanceof TaskletsFlow) {
                if (((TaskletsFlow) parent).getMachine() != null) {
                    return parent;
                } else {
                    parent = parent.getParent();
                }
            } else if (parent instanceof FeatureFlow) {
                return parent;
            } else {
                return null;
            }
        }
    }

    public void setResource(Resource resource) {
        if (this.machine != null) {
            this.allocatedResource = resource;
        } else {
            IDataModel parent = findEnclosedMachineHolder();
            if (parent instanceof TaskletsFlow) {
                ((TaskletsFlow) parent).setResource(resource);
            } else if (parent instanceof FeatureFlow) {
                ((FeatureFlow) parent).setResource(resource);
            }
        }
    }

    public Resource getSelfResource() {
        return this.allocatedResource;
    }

    public Resource getResource() {
        if (this.machine != null) {
            return this.allocatedResource;
        } else {
            IDataModel parent = findEnclosedMachineHolder();
            if (parent instanceof TaskletsFlow) {
                return ((TaskletsFlow) parent).getResource();
            } else if (parent instanceof FeatureFlow) {
                return ((FeatureFlow) parent).getResource();
            }
        }
        return null;
    }

    public void setItems(Items items) {
        this.items = items;
    }

    public Items getItems() {
        return this.items;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOnFail() {
        return onFail;
    }

    public FailurePolicy getFailurePolicy(Evaluator eval) {
        String policy = eval.eval(onFail);
        if (policy.equals("exit"))
            return FailurePolicy.Exit;
        if (policy.equals("ignore"))
            return FailurePolicy.Ignore;
        if (policy.equals("redo"))
            return FailurePolicy.Reschedule;
        return FailurePolicy.Exit;
    }

    public void setOnFail(String onFail) {
        this.onFail = onFail;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
