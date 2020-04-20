package com.intel.cedar.engine.model;

import com.intel.cedar.engine.model.event.ModelAdapterFactoryRegistry;

public class DataModelDocument extends DataModel implements IDataModelDocument {
    private ModelAdapterFactoryRegistry factoryRegistry;

    public DataModelDocument() {
        super(null);
    }

    public ModelAdapterFactoryRegistry getFactoryRegistry() {
        if (factoryRegistry == null) {
            factoryRegistry = new ModelAdapterFactoryRegistry();
        }
        return factoryRegistry;
    }
}
