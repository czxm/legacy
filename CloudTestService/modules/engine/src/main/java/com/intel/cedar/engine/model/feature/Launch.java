package com.intel.cedar.engine.model.feature;

import com.intel.cedar.engine.model.DataModel;
import com.intel.cedar.engine.model.IDataModelDocument;

public class Launch extends DataModel {
    private Variables variables;

    public Launch(IDataModelDocument document) {
        super(document);
        this.variables = new Variables(document);
    }

    public Variables getVariables() {
        return variables;
    }

    public void setVariables(Variables variables) {
        this.variables = variables;
    }
}
