package com.intel.cedar.agent.impl;

import java.util.Properties;

import com.intel.cedar.service.client.feature.model.Variable;

public class CedarRequest {
    private CedarAction action;
    private Object payload;

    private CedarRequest(CedarAction action, Object payload) {
        this.action = action;
        this.payload = payload;
    }

    public static CedarRequest newReadVariableRequest(String name) {
        return new CedarRequest(CedarAction.ReadVariable, name);
    }

    public static CedarRequest newWriteVariableRequest(Variable var) {
        return new CedarRequest(CedarAction.WriteVariable, var);
    }

    public static CedarRequest newGetFeaturePropertyRequest(String name,
            String version) {
        Properties props = new Properties();
        props.setProperty("_CEDAR_GET_FEATURE_PROPERTY_KEY", name);
        if (version != null)
            props.setProperty("_CEDAR_FEATURE_VERSION", version);
        return new CedarRequest(CedarAction.GetFeatureProperty, props);
    }

    public static CedarRequest newSetFeaturePropertyRequest(String key,
            String value, String version) {
        Properties props = new Properties();
        props.setProperty("_CEDAR_SET_FEATURE_PROPERTY_KEY", key);
        props.setProperty("_CEDAR_SET_FEATURE_PROPERTY_VALUE", value);
        if (version != null)
            props.setProperty("_CEDAR_FEATURE_VERSION", version);
        return new CedarRequest(CedarAction.SetFeatureProperty, props);
    }

    public static CedarRequest newGetFeaturePropertiesRequest(String version) {
        Properties props = new Properties();
        if (version != null)
            props.setProperty("_CEDAR_FEATURE_VERSION", version);
        return new CedarRequest(CedarAction.GetFeatureProperties, props);
    }

    public static CedarRequest newSetFeaturePropertiesRequest(Properties props,
            String version) {
        if (version != null)
            props.setProperty("_CEDAR_FEATURE_VERSION", version);
        return new CedarRequest(CedarAction.SetFeatureProperties, props);
    }

    public static CedarRequest newReadStorageFileRequest(String url) {
        return new CedarRequest(CedarAction.ReadStorageFile, url);
    }

    public static CedarRequest newWriteStorageFileRequest(String url) {
        return new CedarRequest(CedarAction.WriteStorageFile, url);
    }

    public static CedarRequest newCreateStorageFolderRequest(String url) {
        return new CedarRequest(CedarAction.CreateStorageFolder, url);
    }

    public static CedarRequest newDeleteStorageFolderRequest(String url) {
        return new CedarRequest(CedarAction.DeleteStorageFolder, url);
    }

    public static CedarRequest newCreateStorageFileRequest(String url) {
        return new CedarRequest(CedarAction.CreateStorageFile, url);
    }

    public static CedarRequest newDeleteStorageFileRequest(String url) {
        return new CedarRequest(CedarAction.DeleteStorageFile, url);
    }

    public static CedarRequest newStorageFileExistRequest(String url) {
        return new CedarRequest(CedarAction.StorageFileExist, url);
    }

    public static CedarRequest newStorageFolderExistRequest(String url) {
        return new CedarRequest(CedarAction.StorageFolderExist, url);
    }
    
    public static CedarRequest newStorageListRequest(String url) {
        return new CedarRequest(CedarAction.StorageList, url);
    }
    
    public static CedarRequest newStorageLengthRequest(String url) {
        return new CedarRequest(CedarAction.StorageLength, url);
    }
    
    public static CedarRequest newStorageLastModifiedRequest(String url) {
        return new CedarRequest(CedarAction.StorageLastModified, url);
    }

    public CedarAction getAction() {
        return this.action;
    }

    public Object getPayload() {
        return this.payload;
    }
}
