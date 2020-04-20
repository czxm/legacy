package com.intel.cedar.engine.model;

import com.intel.cedar.engine.model.event.ModelAdapterFactoryRegistry;

public interface IDataModelDocument extends IDataModel {
    public ModelAdapterFactoryRegistry getFactoryRegistry();
}
