package com.intel.cedar.engine.model.event;

public class PropertyChangeEvent extends ChangeEvent {
    protected String property;
    protected Object oldValue;
    protected Object newValue;

    public PropertyChangeEvent(Object source, String property, Object oldValue,
            Object newValue) {
        super(source);
        this.property = property;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    /**
     * Get the property changed
     */
    public String getProperty() {
        return property;
    }

    /**
     * Get the old value
     */
    public Object getOldValue() {
        return oldValue;
    }

    /**
     * Get the new value
     */
    public Object getNewValue() {
        return newValue;
    }

}
