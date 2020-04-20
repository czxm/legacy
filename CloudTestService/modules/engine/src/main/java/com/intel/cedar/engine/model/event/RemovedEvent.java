package com.intel.cedar.engine.model.event;

import com.intel.cedar.engine.model.IDataModel;

public class RemovedEvent extends ChangeEvent {
    private IDataModel oldParent;

    public RemovedEvent(Object source, IDataModel oldParent) {
        super(source);
        this.oldParent = oldParent;
    }

    /**
     * Get the old parent
     */
    public IDataModel getOldParent() {
        return oldParent;
    }
}
