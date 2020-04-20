package com.intel.cedar.agent.impl;

import java.util.Properties;

import com.intel.cedar.service.client.feature.model.Variable;
import com.intel.cedar.storage.IStorage;

public class CedarResponse {
    private CedarAction action;
    private Object payload;

    private CedarResponse(CedarAction action, Object payload) {
        this.action = action;
        this.payload = payload;
    }

    public static CedarResponse newReadVariableResponse(Variable var) {
        return new CedarResponse(CedarAction.ReadVariable, var);
    }

    public static CedarResponse newWriteVariableResponse(Variable var) {
        return new CedarResponse(CedarAction.WriteVariable, var);
    }

    public static CedarResponse newGetFeaturePropertyResponse(String value) {
        return new CedarResponse(CedarAction.GetFeatureProperty, value);
    }

    public static CedarResponse newSetFeaturePropertyResponse(Boolean ret) {
        return new CedarResponse(CedarAction.SetFeatureProperty, ret);
    }

    public static CedarResponse newGetFeaturePropertiesResponse(Properties props) {
        return new CedarResponse(CedarAction.GetFeatureProperties, props);
    }

    public static CedarResponse newSetFeaturePropertiesResponse(Boolean ret) {
        return new CedarResponse(CedarAction.SetFeatureProperties, ret);
    }

    public static CedarResponse newReadStorageFileResponse(Boolean ret) {
        return new CedarResponse(CedarAction.ReadStorageFile, ret);
    }

    public static CedarResponse newWriteStorageFileResponse(Boolean ret) {
        return new CedarResponse(CedarAction.WriteStorageFile, ret);
    }

    public static CedarResponse newCreateStorageFileResponse(Boolean ret) {
        return new CedarResponse(CedarAction.CreateStorageFile, ret);
    }

    public static CedarResponse newCreateStorageFolderResponse(Boolean ret) {
        return new CedarResponse(CedarAction.CreateStorageFolder, ret);
    }

    public static CedarResponse newDeleteStorageFileResponse(Boolean ret) {
        return new CedarResponse(CedarAction.DeleteStorageFile, ret);
    }

    public static CedarResponse newDeleteStorageFolderResponse(Boolean ret) {
        return new CedarResponse(CedarAction.DeleteStorageFolder, ret);
    }

    public static CedarResponse newStorageFileExistResponse(Boolean ret) {
        return new CedarResponse(CedarAction.StorageFileExist, ret);
    }

    public static CedarResponse newStorageFolderExistResponse(Boolean ret) {
        return new CedarResponse(CedarAction.StorageFolderExist, ret);
    }
    
    public static CedarResponse newStorageLengthResponse(Long ret) {
        return new CedarResponse(CedarAction.StorageLength, ret);
    }
    
    public static CedarResponse newStorageLastModifiedResponse(Long ret) {
        return new CedarResponse(CedarAction.StorageLength, ret);
    }
    
    public static CedarResponse newStorageListResponse(IStorage[] ret) {
        String[] urls = new String[ret.length];
        for(int i = 0; i < ret.length; i++){
            urls[i] = ret[i].getURI().toString();
        }
        return new CedarResponse(CedarAction.StorageList, urls);
    }

    public CedarAction getAction() {
        return this.action;
    }

    public Object getPayload() {
        return this.payload;
    }

    public boolean isSucceeded() {
        return !(payload instanceof Exception);
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public boolean isReadVariableResponse() {
        return action.equals(CedarAction.ReadVariable);
    }

    public boolean isWriteVariableResponse() {
        return action.equals(CedarAction.WriteVariable);
    }

    public boolean isReadStorageFileResponse() {
        return action.equals(CedarAction.ReadStorageFile);
    }

    public boolean isWriteStorageFileResponse() {
        return action.equals(CedarAction.WriteStorageFile);
    }

    public boolean isCreateStorageFileResponse() {
        return action.equals(CedarAction.CreateStorageFile);
    }

    public boolean isDeleteStorageFileResponse() {
        return action.equals(CedarAction.DeleteStorageFile);
    }

    public boolean isCreateStorageFolderResponse() {
        return action.equals(CedarAction.CreateStorageFolder);
    }

    public boolean isDeletStorageFolderResponse() {
        return action.equals(CedarAction.DeleteStorageFolder);
    }

    public boolean isStorageFolderExistResponse() {
        return action.equals(CedarAction.StorageFolderExist);
    }

    public boolean isStorageFileExistResponse() {
        return action.equals(CedarAction.StorageFileExist);
    }

    public boolean isGetFeaturePropertiesResponse() {
        return action.equals(CedarAction.GetFeatureProperties);
    }

    public boolean isGetFeaturePropertyResponse() {
        return action.equals(CedarAction.GetFeatureProperty);
    }

    public boolean isSetFeaturePropertiesResponse() {
        return action.equals(CedarAction.SetFeatureProperties);
    }

    public boolean isSetFeaturePropertyResponse() {
        return action.equals(CedarAction.SetFeatureProperty);
    }
}
