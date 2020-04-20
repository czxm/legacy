package com.intel.cedar.service.client.feature.view;

import java.util.ArrayList;

import com.extjs.gxt.ui.client.data.Loader;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.layout.LayoutData;
import com.intel.cedar.service.client.feature.model.ActionWidget;
import com.intel.cedar.service.client.feature.model.EventWidget;
import com.intel.cedar.service.client.feature.model.ui.DependsAction;
import com.intel.cedar.service.client.feature.model.ui.UIBaseNode;

public class TypedWidget {
    private Component widget;
    private Loader loader;
    private ListStore store;
    private LayoutData layoutData;
    private UIType type;
    private UIBaseNode uiModel;
    private TypedWidget parent;
    private ArrayList<TypedWidget> children = new ArrayList<TypedWidget>();
    private ArrayList<ActionWidget> deps = new ArrayList<ActionWidget>();
    private ArrayList<EventWidget> refs = new ArrayList<EventWidget>();

    public TypedWidget() {

    }

    public TypedWidget(Component widget, UIBaseNode uiModel,
            LayoutData layoutData, UIType type) {
        this.setUiModel(uiModel);
        this.setWidget(widget);
        this.setLayoutData(layoutData);
        this.setType(type);
    }

    public void setWidget(Component widget) {
        this.widget = widget;
    }

    public Component getWidget() {
        return widget;
    }

    public void setLayoutData(LayoutData layoutData) {
        this.layoutData = layoutData;
    }

    public LayoutData getLayoutData() {
        return layoutData;
    }

    public void setType(UIType type) {
        this.type = type;
    }

    public UIType getType() {
        return type;
    }

    public void setUiModel(UIBaseNode uiModel) {
        this.uiModel = uiModel;
    }

    public UIBaseNode getUiModel() {
        return uiModel;
    }

    public void setParent(TypedWidget parent) {
        this.parent = parent;
    }

    public TypedWidget getParent() {
        return parent;
    }

    public void addChild(TypedWidget typedWidget) {
        if (typedWidget != null) {
            typedWidget.setParent(this);
        }
        children.add(typedWidget);
    }

    public ArrayList<TypedWidget> getChildren() {
        return children;
    }

    public void setLoader(Loader loader) {
        this.loader = loader;
    }

    public Loader getLoader() {
        return loader;
    }

    public void addRef(EventType eType, TypedWidget ref) {
        EventWidget ew = new EventWidget(eType, ref);
        refs.add(ew);
    }

    public void addRef(EventWidget ew) {
        refs.add(ew);
    }

    public void addDep(DependsAction action, TypedWidget dep) {
        ActionWidget aw = new ActionWidget(action, dep);
        deps.add(aw);
    }

    public void addDep(ActionWidget aw) {
        deps.add(aw);
    }

    public ArrayList<EventWidget> getRefs() {
        return refs;
    }

    public ArrayList<ActionWidget> getDeps() {
        return deps;
    }

    public void setStore(ListStore store) {
        this.store = store;
    }

    public ListStore getStore() {
        return store;
    }

    public String toString() {
        return getType().name();
    }
}
