package com.intel.cedar.engine.model.event;

import com.intel.cedar.engine.model.IDataModel;

public class ChildRemoveEvent extends ChildChangeEvent {

    public ChildRemoveEvent(Object source, int index, IDataModel oldChild) {
        super(source, index, oldChild, null);
    }
}
