package com.intel.cedar.storage.impl;

import java.net.URI;

import com.intel.cedar.feature.TaskRunnerEnvironment;
import com.intel.cedar.storage.IFile;
import com.intel.cedar.storage.IFolder;
import com.intel.cedar.storage.IStorage;
import com.intel.cedar.util.Utils;

public class RemoteFolder implements IFolder {
    private URI uri;
    private RemoteFolder root;

    public RemoteFolder(URI uri) {
        this.uri = uri;
        this.root = this;
    }

    public RemoteFolder(URI uri, RemoteFolder root) {
        this.uri = uri;
        this.root = root;
    }

    @Override
    public IFile getFile(String name) {
        String strURI = Utils.encodeURL(uri.toString()) + Utils.encodeURL(name);
        return new RemoteFile(URI.create(strURI), root);
    }

    @Override
    public IFolder getFolder(String name) {
        String strURI = Utils.encodeURL(uri.toString()) + Utils.encodeURL(name) + "%2F";
        return new RemoteFolder(URI.create(strURI), root);
    }

    @Override
    public boolean create() {
        TaskRunnerEnvironment env = TaskRunnerEnvironment.getInstance();
        return env.createStorageFolder(this);
    }

    @Override
    public boolean delete() {
        TaskRunnerEnvironment env = TaskRunnerEnvironment.getInstance();
        return env.deleteStorageFolder(this);
    }

    @Override
    public IFolder getRoot() {
        return root;
    }

    @Override
    public URI getURI() {
        return uri;
    }

    @Override
    public IFile getFile(URI uri) {
        return new RemoteFile(uri, root);
    }

    @Override
    public IFolder getFolder(URI uri) {
        return new RemoteFolder(uri, root);
    }

    @Override
    public boolean exist() {
        TaskRunnerEnvironment env = TaskRunnerEnvironment.getInstance();
        return env.storageExist(this);
    }

    @Override
    public String getName() {
        String path = getURI().getPath();
        if (path == null)
            return "";
        else
            return path.substring(path.lastIndexOf("/") + 1);
    }

	@Override
	public IStorage[] list() {
        TaskRunnerEnvironment env = TaskRunnerEnvironment.getInstance();
        return env.storageList(this);
	}
	
	@Override
	public long lastModified() {
        TaskRunnerEnvironment env = TaskRunnerEnvironment.getInstance();
        return env.storageLastModified(this);
	}
}
