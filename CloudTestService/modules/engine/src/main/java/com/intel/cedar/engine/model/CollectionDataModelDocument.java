package com.intel.cedar.engine.model;

import com.intel.cedar.engine.model.event.ModelAdapterFactoryRegistry;

public class CollectionDataModelDocument<T> extends CollectionDataModel<T>
        implements IDataModelDocument {
    private ModelAdapterFactoryRegistry factoryRegistry;

    public CollectionDataModelDocument() {
        super(null);
    }

    public ModelAdapterFactoryRegistry getFactoryRegistry() {
        if (factoryRegistry == null) {
            factoryRegistry = new ModelAdapterFactoryRegistry();
        }
        return factoryRegistry;
    }

}
