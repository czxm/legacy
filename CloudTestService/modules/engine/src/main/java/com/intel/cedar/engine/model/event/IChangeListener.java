package com.intel.cedar.engine.model.event;

public interface IChangeListener {
    /**
     * Notify a change event from a notifier
     * 
     * @param change
     *            event
     */
    public void notifyChange(ChangeEvent evt);
}
