package com.intel.cedar.engine.model.feature.flow;

import com.intel.cedar.engine.model.CollectionDataModel;
import com.intel.cedar.engine.model.IDataModelDocument;

public class Items extends CollectionDataModel<Item> {
    private String provider;

    public Items(IDataModelDocument document) {
        super(document);
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getProvider() {
        return this.provider;
    }
}
