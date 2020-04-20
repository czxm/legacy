package com.intel.cedar.engine.model.feature;

import java.util.List;

import com.intel.cedar.engine.model.DataModel;
import com.intel.cedar.engine.model.IDataModelDocument;

public class ResultMetaData extends DataModel {
    private List<TaskletMetaData> tasklets;

    public ResultMetaData(IDataModelDocument document) {
        super(document);
        // TODO Auto-generated constructor stub
    }

}
