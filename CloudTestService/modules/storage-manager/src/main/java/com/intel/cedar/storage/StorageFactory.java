package com.intel.cedar.storage;

import java.util.HashMap;

import com.intel.cedar.storage.impl.CedarStorage;

public class StorageFactory {
    private static HashMap<String, Class<?>> supportedStorages;
    private static HashMap<String, IStorage> storages;
    private static StorageFactory singleton;

    public static synchronized StorageFactory getInstance() {
        if (singleton == null)
            singleton = new StorageFactory();
        return singleton;
    }

    private StorageFactory() {
        supportedStorages = new HashMap<String, Class<?>>();
        storages = new HashMap<String, IStorage>();
        supportedStorages.put("CedarStorage", CedarStorage.class);
    }

    protected IStorage createStorage(String storage) {
        Class<?> clz = (Class<?>) supportedStorages.get(storage);
        if (clz != null) {
            try {
                return (IStorage) clz.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("can't create storage");
            }
        } else
            throw new RuntimeException("can't create storage");
    }

    public IStorage getStorage(String storage) {
        if (!storages.containsKey(storage))
            storages.put(storage, createStorage(storage));
        return storages.get(storage);
    }

    public IStorage getStorage() {
        return getStorage("CedarStorage");
    }
}
