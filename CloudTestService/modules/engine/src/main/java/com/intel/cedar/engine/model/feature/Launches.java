package com.intel.cedar.engine.model.feature;

import com.intel.cedar.engine.model.CollectionDataModel;
import com.intel.cedar.engine.model.IDataModelDocument;

public class Launches extends CollectionDataModel<LaunchSet> {
    public Launches(IDataModelDocument document) {
        super(document);
    }

    public LaunchSet getLaunchSetByName(String name) {
        for (LaunchSet ls : this.getModelChildren()) {
            if (ls.getName().equals(name))
                return ls;
        }
        return null;
    }
}
