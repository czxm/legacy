package com.intel.cedar.service.client.feature.model.ui;

import java.util.ArrayList;
import java.util.List;

import com.intel.cedar.service.client.feature.model.uitl.UIUtils;

public abstract class UIBaseNodes extends UIBaseNode {

    protected List<UIBaseNode> children = new ArrayList<UIBaseNode>();
    private int iter = 0;

    private static final long serialVersionUID = UIUtils
            .getSerialVUID("UIBaseNodes");

    public UIBaseNodes() {
        super();
    }

    public void addChild(UIBaseNode child) {
        children.add(child);
        child.setParent(this);
    }

    public List<UIBaseNode> getChildren() {
        return this.children;
    }

    @Override
    final public boolean isContainer() {
        return true;
    }

    @Override
    public void init() {
        super.init();
        for (int i = 0; i < children.size(); i++) {
            UIBaseNode child = children.get(i);
            child.init();
        }
    }

    @Override
    public IUINode nextChild() {
        if (iter >= children.size()) {
            return null;
        }

        return children.get(iter++);
    }

    public void resetIter() {
        iter = 0;
    }
}
