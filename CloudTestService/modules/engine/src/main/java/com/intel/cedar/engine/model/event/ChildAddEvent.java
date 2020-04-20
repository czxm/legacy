package com.intel.cedar.engine.model.event;

import com.intel.cedar.engine.model.IDataModel;

public class ChildAddEvent extends ChildChangeEvent {

    public ChildAddEvent(Object source, int index, IDataModel newChild) {
        super(source, index, null, newChild);
    }

}
