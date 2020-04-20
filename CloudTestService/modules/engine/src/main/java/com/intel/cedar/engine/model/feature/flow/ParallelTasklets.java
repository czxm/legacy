package com.intel.cedar.engine.model.feature.flow;

import com.intel.cedar.engine.model.IDataModelDocument;

public class ParallelTasklets extends TaskletsFlow {
    private String level;

    public ParallelTasklets(IDataModelDocument document) {
        super(document);
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getLevel() {
        return this.level;
    }
}
