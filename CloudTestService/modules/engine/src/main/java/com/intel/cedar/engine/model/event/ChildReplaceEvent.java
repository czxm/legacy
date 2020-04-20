package com.intel.cedar.engine.model.event;

import com.intel.cedar.engine.model.IDataModel;

public class ChildReplaceEvent extends ChildChangeEvent {

    public ChildReplaceEvent(Object source, int index, IDataModel oldChild,
            IDataModel newChild) {
        super(source, index, oldChild, newChild);
    }

}
