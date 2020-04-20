package com.intel.cedar.service.client.feature.model;

import com.extjs.gxt.ui.client.event.EventType;
import com.intel.cedar.service.client.feature.view.TypedWidget;

public class EventWidget {
    private EventType eType;
    private TypedWidget widget;

    public EventWidget() {

    }

    public EventWidget(EventType eType, TypedWidget widget) {
        this.seteType(eType);
        this.setTWidget(widget);
    }

    public void seteType(EventType eType) {
        this.eType = eType;
    }

    public EventType geteType() {
        return eType;
    }

    public void setTWidget(TypedWidget widget) {
        this.widget = widget;
    }

    public TypedWidget getTWidget() {
        return widget;
    }

}
