package com.intel.cedar.engine.model.feature;

import com.intel.cedar.engine.model.CollectionDataModel;
import com.intel.cedar.engine.model.IDataModelDocument;

public class Shortcuts extends CollectionDataModel<Shortcut> {
    private Variables variables;

    public Shortcuts(IDataModelDocument document) {
        super(document);
        variables = new Variables(document);
    }

    public Variables getVariables() {
        return variables;
    }

    public void setVariables(Variables variables) {
        this.variables = variables;
    }
}
