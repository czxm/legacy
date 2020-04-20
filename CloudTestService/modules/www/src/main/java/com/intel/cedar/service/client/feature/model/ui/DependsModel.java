package com.intel.cedar.service.client.feature.model.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.intel.cedar.service.client.feature.model.uitl.UIUtils;

public class DependsModel implements Serializable {
    private UIBaseNode current;
    private List<DependModel> depends = new ArrayList<DependModel>();

    private static final long serialVersionUID = UIUtils
            .getSerialVUID("DependsModel");

    public DependsModel() {
    }

    public DependsModel(UIBaseNode current) {
        this.current = current;
    }

    public DependModel addDepend(String dependID) {
        DependModel depend = new DependModel(current, dependID);
        this.depends.add(depend);
        return depend;
    }

    public void removeDepend(DependModel depend) {
        if (depend == null) {
            return;
        }
        this.depends.remove(depend);
    }

    public List<DependModel> getDepends() {
        List<DependModel> l = new ArrayList<DependModel>();
        for (DependModel dm : depends) {
            if (dm.getDepend() != null) {
                l.add(dm);
            }
        }
        return l;
    }

    public List<IUINode> getDependNodes() {
        List<IUINode> dependNodes = new ArrayList<IUINode>();
        for (int i = 0; i < depends.size(); i++) {
            DependModel dm = depends.get(i);
            IUINode e = dm.getDepend();
            if (e != null) {
                dependNodes.add(e);
            }
        }

        return dependNodes;
    }

    public DependsAction getDependsAction(IUINode node) {
        for (DependModel model : depends) {
            if (node.getID().equals(model.getDependID())
                    || node == model.getDepend()) {
                return model.getAction();
            }
        }

        return null;
    }

    public DependModel getDependModel(IUINode node) {
        for (DependModel model : depends) {
            if (node.getID().equals(model.getDependID())
                    || node == model.getDepend()) {
                return model;
            }
        }

        return null;
    }

}
