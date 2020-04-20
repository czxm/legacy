package com.intel.cedar.engine.model.feature.flow;

import com.intel.cedar.engine.model.DataModel;
import com.intel.cedar.engine.model.IDataModelDocument;

public class Tag extends DataModel {
    private String value;

    public Tag(IDataModelDocument document) {
        super(document);
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

}
