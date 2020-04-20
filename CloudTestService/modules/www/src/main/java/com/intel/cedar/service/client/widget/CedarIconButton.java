package com.intel.cedar.service.client.widget;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.widget.button.IconButton;
import com.google.gwt.user.client.Event;

public class CedarIconButton extends IconButton {
    public CedarIconButton(String style) {
        super(style);
    }

    public void onComponentEvent(ComponentEvent ce) {
        switch (ce.getEventTypeInt()) {
        case Event.ONMOUSEOVER:
            removeStyleName(style);
            addStyleName(style + "-over");
            break;
        case Event.ONMOUSEOUT:
            removeStyleName(style + "-over");
            addStyleName(style);
            break;
        case Event.ONCLICK:
            onClick(ce);
            break;
        case Event.ONFOCUS:
            onFocus(ce);
            break;
        case Event.ONBLUR:
            onBlur(ce);
            break;
        }
    }

    protected void onClick(ComponentEvent ce) {
        if (cancelBubble) {
            ce.cancelBubble();
        }

        fireEvent(Events.Select, ce);
    }

}
