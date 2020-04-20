package com.intel.cedar.service.client.widget;

import com.extjs.gxt.ui.client.widget.Label;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;

public class CedarLabel extends Label {

    public CedarLabel() {
        super();
    }

    public CedarLabel(String text) {
        super(text);
    }

    public void onRender(Element target, int index) {
        super.onRender(target, index);
        sinkEvents(Event.ONCLICK | Event.MOUSEEVENTS);
    }
}
