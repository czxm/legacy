package com.intel.cedar.service.client.feature.model;

import com.intel.cedar.service.client.feature.model.ui.DependsAction;
import com.intel.cedar.service.client.feature.view.TypedWidget;

public class ActionWidget {
    private DependsAction action;
    private TypedWidget tWidget;

    public ActionWidget() {

    }

    public ActionWidget(DependsAction action, TypedWidget tWidget) {
        this.setAction(action);
        this.setTWidget(tWidget);
    }

    public void setAction(DependsAction action) {
        this.action = action;
    }

    public DependsAction getAction() {
        return action;
    }

    public void setTWidget(TypedWidget tWidget) {
        this.tWidget = tWidget;
    }

    public TypedWidget getTWidget() {
        return tWidget;
    }
}
