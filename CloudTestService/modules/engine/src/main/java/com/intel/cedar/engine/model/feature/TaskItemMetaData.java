package com.intel.cedar.engine.model.feature;

import com.intel.cedar.engine.model.DataModel;
import com.intel.cedar.engine.model.IDataModelDocument;

public class TaskItemMetaData extends DataModel {
    private String logURI;
    private String status;
    private String errorMessage;

    private String volumeId;
    private String machineId;

    public TaskItemMetaData(IDataModelDocument document) {
        super(document);
        // TODO Auto-generated constructor stub
    }

    public String getLogURI() {
        return logURI;
    }

    public void setLogURI(String logURI) {
        this.logURI = logURI;
    }

    public String getVolumeId() {
        return volumeId;
    }

    public void setVolumeId(String volumeId) {
        this.volumeId = volumeId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}
