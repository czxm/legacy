package com.intel.cedar.engine.model.feature;

import java.util.Iterator;

import com.intel.cedar.engine.model.DataModelDocument;
import com.intel.cedar.engine.model.IDataModel;
import com.intel.cedar.engine.util.MonoIterator;

public class FeatureDoc extends DataModelDocument {
    private Feature feature;

    public FeatureDoc() {
        feature = new Feature(this);
    }

    public Feature getFeature() {
        return this.feature;
    }

    public Iterator<IDataModel> iterate() {
        return new MonoIterator<IDataModel>(feature);
    }
}
