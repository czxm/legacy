package com.intel.cedar.engine.model.event;

import com.intel.cedar.engine.model.IDataModel;

public class ChildChangeEvent extends ChangeEvent {
    private int index;
    private IDataModel oldChild;
    private IDataModel newChild;

    /**
     * Constructs a new <code>ChildChangeEvent</code>.
     * 
     * @param source
     *            The bean that fired the event.
     */
    public ChildChangeEvent(Object source, int index, IDataModel oldChild,
            IDataModel newChild) {
        super(source);
        this.index = index;
        this.oldChild = oldChild;
        this.newChild = newChild;
    }

    /**
     * Get the index of the child that changed
     */
    public int getIndex() {
        return index;
    }

    /**
     * Get the old value
     */
    public IDataModel getOldChild() {
        return oldChild;
    }

    /**
     * Get the new value
     */
    public IDataModel getNewChild() {
        return newChild;
    }

}
