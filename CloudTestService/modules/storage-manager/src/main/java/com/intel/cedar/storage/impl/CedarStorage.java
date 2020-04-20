package com.intel.cedar.storage.impl;

import java.io.File;
import java.net.URI;

import com.intel.cedar.feature.util.FileUtils;
import com.intel.cedar.storage.IFolder;
import com.intel.cedar.storage.IStorage;
import com.intel.cedar.util.BaseDirectory;
import com.intel.cedar.util.CedarConfiguration;
import com.intel.cedar.util.NetUtil;
import com.intel.cedar.util.Utils;

public class CedarStorage implements IStorage {
    private File storageRoot;
    private IFolder rootFolder;

    public CedarStorage(String root) {
        this.storageRoot = new File(root);
        if (!storageRoot.exists())
            storageRoot.mkdir();
    }

    public CedarStorage() {
        String root = CedarConfiguration.getInstance().getStorageRoot();
        if (root == null)
            root = BaseDirectory.HOME.toString() + "storage";
        storageRoot = new File(root);
    }

    @Override
    public boolean create() {
        if (!storageRoot.exists())
            return storageRoot.mkdir();
        return false;
    }

    @Override
    public boolean delete() {
        if (storageRoot.exists()) {
            FileUtils.deleteFolderAndContents(storageRoot);
            return true;
        }
        return false;
    }

    @Override
    public IFolder getRoot() {
        if (rootFolder == null)
            rootFolder = new LocalFolder(storageRoot, this);
        return rootFolder;
    }

    @Override
    public URI getURI() {
        String url = "cedar://" + NetUtil.getHostName() + "/";
        return URI.create(Utils.encodeURL(url));
    }

    @Override
    public boolean exist() {
        return storageRoot.exists();
    }

    @Override
    public String getName() {
        return "/";
    }
    
    @Override
    public long lastModified() {
    	return storageRoot.lastModified();
    }
}
