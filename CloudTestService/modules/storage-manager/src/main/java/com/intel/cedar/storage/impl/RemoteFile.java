package com.intel.cedar.storage.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import com.intel.cedar.feature.TaskRunnerEnvironment;
import com.intel.cedar.feature.util.FileUtils;
import com.intel.cedar.storage.IFile;
import com.intel.cedar.storage.IFolder;

public class RemoteFile implements IFile {
    private File attachedFile;
    private URI uri;
    private RemoteFolder root;

    public RemoteFile(URI uri, RemoteFolder root) {
        this.uri = uri;
        this.root = root;
        this.attachedFile = null;
    }

    @Override
    public InputStream getContents() throws Exception {
        if (this.attachedFile == null) {
            File tmpFile = File.createTempFile("cedar", "storage");
            TaskRunnerEnvironment env = TaskRunnerEnvironment.getInstance();
            env.loadStorage(tmpFile, this);
            this.attachedFile = tmpFile;
            tmpFile.deleteOnExit();
        }
        return new FileInputStream(attachedFile);
    }

    @Override
    public void setContents(InputStream source) throws Exception {
        if (this.attachedFile == null) {
            File tmpFile = File.createTempFile("cedar", "storage");
            this.attachedFile = tmpFile;
            tmpFile.deleteOnExit();
        }
        OutputStream output = new FileOutputStream(attachedFile);
        FileUtils.copyStream(source, output);
        output.close();
        TaskRunnerEnvironment env = TaskRunnerEnvironment.getInstance();
        env.saveStorage(attachedFile, this);
    }

    @Override
    public boolean create() {
        TaskRunnerEnvironment env = TaskRunnerEnvironment.getInstance();
        return env.createStorageFile(this);
    }

    @Override
    public boolean delete() {
        TaskRunnerEnvironment env = TaskRunnerEnvironment.getInstance();
        return env.deleteStorageFile(this);
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
    public long length() {
        TaskRunnerEnvironment env = TaskRunnerEnvironment.getInstance();
        return env.storageLength(this);
    }
    
    @Override
    public long lastModified() {
        TaskRunnerEnvironment env = TaskRunnerEnvironment.getInstance();
        return env.storageLastModified(this);   	
    }
}
