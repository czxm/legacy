package com.intel.cedar.engine.model.event;

import java.util.EventObject;

/**
 * The base class of all expression change event
 */
public class ChangeEvent extends EventObject {
    /**
     * Constructs a new <code>ExpressionChangeEvent</code>.
     * 
     * @param source
     *            The bean that fired the event.
     */
    public ChangeEvent(Object source) {
        super(source);
    }
}
