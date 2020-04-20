package com.intel.cedar.service.client.feature.model.ui;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.intel.cedar.service.client.feature.model.uitl.UIUtils;
import com.intel.cedar.service.client.feature.view.UIBuilder;

public abstract class UIBaseNode extends BaseModel implements IUINode,
        IDependsModelProvider {
    protected String ID;
    protected String label;
    protected IUINode parent;
    protected DependsModel depends;
    protected String showOnSelectValue;
    protected boolean isShow;
    protected boolean isStaticWidget;
    protected FeatureModel featureUI;

    private static final long serialVersionUID = UIUtils
            .getSerialVUID("UIBaseNode");

    protected static List<String> EMPTY = new ArrayList<String>();

    public UIBaseNode() {
        isShow = true;
        isStaticWidget = true;
        depends = new DependsModel(this);
    }

    public FeatureModel getFeature() {
        if (featureUI == null) {
            computeFeatureUI();
        }

        return featureUI;
    }

    @Override
    public void init() {
        computeShow();
    }

    @Override
    public void accept(UIBuilder builder) {
        // DO nothing
    }

    @Override
    public void setID(String id) {
        this.ID = id;
    }

    @Override
    public String getID() {
        return this.ID;
    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public void setParent(IUINode parent) {
        this.parent = parent;
    }

    @Override
    public IUINode getParent() {
        return parent;
    }

    @Override
    public boolean isContainer() {
        return false;
    }

    @Override
    public IUINode nextChild() {
        return null;
    }

    @Override
    public IUINode next() {
        IUINode po = this.getParent();
        if (po == null) {
            return null;
        }

        if (!(po instanceof UIBaseNodes)) {
            return null;
        }

        UIBaseNodes parent = (UIBaseNodes) po;

        List<UIBaseNode> siblings = parent.getChildren();
        for (int i = 0; i < siblings.size(); i++) {
            UIBaseNode current = siblings.get(i);
            int next = i + 1;
            if (current.equals(this) && (next < siblings.size())) {
                return siblings.get(next);
            }
        }

        return null;
    }

    @Override
    public List<IUINode> getSiblings() {
        List<IUINode> siblings = new ArrayList<IUINode>();
        if (parent == null) {
            return siblings;
        }

        if (!(parent instanceof UIBaseNodes)) {
            return siblings;
        }

        List<UIBaseNode> childs = ((UIBaseNodes) parent).getChildren();
        for (int i = 0; i < childs.size(); i++) {
            UIBaseNode o = childs.get(i);
            if (o.equals(this)) {
                continue;
            }

            siblings.add((IUINode) o);
        }
        return siblings;
    }

    @Override
    public boolean isStaticWidget() {
        return isStaticWidget;
    }

    @Override
    public boolean isShow() {
        return isShow;
    }

    @Override
    public void setShowOnSelect(String dependedValue) {
        showOnSelectValue = dependedValue;
        computeShow();
    }

    @Override
    public String getShowOnSelect() {
        return showOnSelectValue;
    }

    @Override
    public List<String> getValues() {
        return EMPTY;
    }

    @Override
    public DependModel addDepend(String dependID) {
        return depends.addDepend(dependID);
    }

    @Override
    public DependsModel getDependsModel() {
        return depends;
    }

    @Override
    public List<IUINode> getDepends() {
        return depends.getDependNodes();
    }

    @Override
    public List<String> getDependsValues() {
        return EMPTY;
    }

    protected void setShow(boolean isShow) {
        boolean oldShow = this.isShow;
        if (isShow == oldShow) {
            return;
        }

        this.isShow = isShow;
    }

    protected void computeShow() {
        if (depends == null || showOnSelectValue == null) {
            return;
        }

        boolean show = false;
        // TODO
        this.setShow(show);
    }

    protected void computeFeatureUI() {
        if (this instanceof FeatureModel) {
            featureUI = (FeatureModel) this;
            return;
        }

        IUINode parent = this.getParent();
        while (parent != null) {
            if (parent instanceof FeatureModel) {
                featureUI = (FeatureModel) parent;
                break;
            }
            parent = parent.getParent();
        }

    }
}
