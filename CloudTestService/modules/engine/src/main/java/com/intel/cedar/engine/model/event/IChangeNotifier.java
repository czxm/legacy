package com.intel.cedar.engine.model.event;

public interface IChangeNotifier {
    /**
     * Add a IChangeListener to this notifier
     * 
     * @param listener
     */
    public void addChangeListener(IChangeListener listener);

    /**
     * Remove a IChangeListener from this notifier
     * 
     * @param listener
     */
    public void removeChangeListener(IChangeListener listener);

}
