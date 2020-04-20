package com.intel.cedar.engine.model.feature.flow;

import com.intel.cedar.engine.model.DataModel;
import com.intel.cedar.engine.model.IDataModelDocument;

public class Item extends DataModel {
    private String provider;

    public Item(IDataModelDocument document) {
        super(document);
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getProvider() {
        return this.provider;
    }
}
