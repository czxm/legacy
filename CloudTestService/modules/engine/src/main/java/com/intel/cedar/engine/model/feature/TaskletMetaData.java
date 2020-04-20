package com.intel.cedar.engine.model.feature;

import java.util.List;

import com.intel.cedar.engine.model.DataModel;
import com.intel.cedar.engine.model.IDataModelDocument;

public class TaskletMetaData extends DataModel {
    private List<TaskItemMetaData> taskItems;

    public TaskletMetaData(IDataModelDocument document) {
        super(document);
        // TODO Auto-generated constructor stub
    }

}
