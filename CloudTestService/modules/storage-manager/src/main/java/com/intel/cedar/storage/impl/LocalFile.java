package com.intel.cedar.storage.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import com.intel.cedar.feature.util.FileUtils;
import com.intel.cedar.storage.IFile;
import com.intel.cedar.storage.IFolder;
import com.intel.cedar.util.Utils;

public class LocalFile implements IFile {
    private File file;
    private CedarStorage storage;

    public LocalFile(File file, CedarStorage storage) {
        this.file = file;
        this.storage = storage;
    }

    public File toFile() {
        return this.file;
    }

    @Override
    public InputStream getContents() throws Exception {
        if (file.exists() && file.isFile())
            return new FileInputStream(file);
        return null;
    }

    @Override
    public void setContents(InputStream source) throws Exception {
        create();
        if (file.exists() && file.isFile()) {
            FileOutputStream output = new FileOutputStream(file);
            FileUtils.copyStream(source, output);
            output.close();
        }
    }

    @Override
    public boolean create() {
        if (!file.exists()) {
            try {
                return file.createNewFile();
            } catch (IOException e) {
            }
        }
        return false;
    }

    @Override
    public boolean delete() {
        if (file.exists() && file.isFile())
            return file.delete();
        return false;
    }

    @Override
    public IFolder getRoot() {
        return storage.getRoot();
    }

    @Override
    public URI getURI() {
        String base = storage.getURI().toString();
        String baseAbsPath = ((LocalFolder) getRoot()).toFile()
                .getAbsolutePath();
        String actualPath = file.getAbsolutePath().replace(baseAbsPath, "");
        actualPath = actualPath.substring(1).replace("\\", "/");
        try{
            actualPath = Utils.encodeURL(actualPath);
            return URI.create(base + actualPath);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean exist() {
        return file.exists();
    }

    @Override
    public String getName() {
        return file.getName();
    }
    
    @Override
    public long length(){
    	return file.length();
    }
    
    @Override
    public long lastModified() {
    	return file.lastModified();
    }
}
