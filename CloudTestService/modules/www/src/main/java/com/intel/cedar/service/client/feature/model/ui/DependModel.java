package com.intel.cedar.service.client.feature.model.ui;

import java.util.ArrayList;
import java.util.List;

import com.intel.cedar.service.client.feature.model.uitl.UIUtils;

public class DependModel implements IDependModel {
    private String dependID;
    private DependsAction action = DependsAction.UNKOWN;
    private IUINode current;
    private IUINode depend;

    private static final long serialVersionUID = UIUtils
            .getSerialVUID("DependModel");
    public static List<String> EMPTY = new ArrayList<String>();

    public DependModel() {
    }

    public DependModel(IUINode current, String id) {
        this.current = current;
        this.dependID = id;
    }

    public DependModel(IUINode current, IUINode depend) {
        this.current = current;
        this.depend = depend;
        this.dependID = depend.getID();
    }

    @Override
    public IUINode getDepend() {
        if (depend == null) {
            computeDepends();
        }
        return depend;
    }

    @Override
    public String getDependID() {
        return dependID;
    }

    @Override
    public List<String> getValues() {
        if (depend == null) {
            return EMPTY;
        }

        return depend.getValues();
    }

    @Override
    public void setDependID(String id) {
        dependID = id;
    }

    @Override
    public DependsAction getAction() {
        return action;
    }

    @Override
    public void setAction(DependsAction action) {
        this.action = action;
    }

    @Override
    public void setAction(String name) {
        this.action = DependsAction.getDependsAction(name);
    }

    protected void computeDepends() {
        if (dependID == null || current == null) {
            return;
        }

        UIBaseNodes root = ((UIBaseNode) current).getFeature();
        depend = search(root);
    }

    protected UIBaseNode search(UIBaseNodes parent) {
        List<UIBaseNode> childs = parent.getChildren();
        for (UIBaseNode child : childs) {
            String id = child.getID();
            if (id != null && id.equals(dependID)) {
                return child;
            }

            if (!(child instanceof UIBaseNodes)) {
                continue;
            }

            UIBaseNode find = search(((UIBaseNodes) child));
            if (find != null) {
                return find;
            }
        }

        return null;
    }
}
