package com.intel.cedar.storage.impl;

import java.io.File;
import java.net.URI;

import com.intel.cedar.feature.util.FileUtils;
import com.intel.cedar.storage.IFile;
import com.intel.cedar.storage.IFolder;
import com.intel.cedar.storage.IStorage;
import com.intel.cedar.util.Utils;

public class LocalFolder implements IFolder {
    private File folder;
    private CedarStorage storage;

    public LocalFolder(File folder, CedarStorage storage) {
        this.folder = folder;
        this.storage = storage;
    }

    @Override
    public IFile getFile(String name) {
        File child = new File(folder, name);
        return new LocalFile(child, storage);
    }

    @Override
    public IFolder getFolder(String name) {
        File child = new File(folder, name);
        return new LocalFolder(child, storage);
    }

    @Override
    public boolean create() {
        if (!folder.exists())
            return folder.mkdir();
        return false;
    }

    @Override
    public boolean delete() {
        if (folder.exists()) {
            FileUtils.deleteFolderAndContents(folder);
            return true;
        }
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
        String actualPath = folder.getAbsolutePath().replace(baseAbsPath, "");
        if (!actualPath.equals("")) {
            actualPath = actualPath.substring(1).replace("\\", "/");
        }
        try{
            if(actualPath.length() > 0){
                actualPath = Utils.encodeURL(actualPath);
                return URI.create(base + actualPath + "%2F");
            }
            else{
                return URI.create(base);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public File toFile() {
        return folder;
    }

    @Override
    public IFile getFile(URI uri) {
        String base = storage.getURI().toString();
        String baseAbsPath = ((LocalFolder) getRoot()).toFile()
                .getAbsolutePath();
        String actualPath = Utils.encodeURL(uri.toString()).replace(base, "").replace("%2F",
                File.separator);
        return new LocalFile(
                new File(baseAbsPath + File.separator + Utils.decodeURL(actualPath)), storage);
    }

    @Override
    public IFolder getFolder(URI uri) {
        String base = storage.getURI().toString();
        String baseAbsPath = ((LocalFolder) getRoot()).toFile()
                .getAbsolutePath();
        String actualPath = Utils.encodeURL(uri.toString()).replace(base, "").replace("%2F",
                File.separator);
        return new LocalFolder(new File(baseAbsPath + File.separator
                + Utils.decodeURL(actualPath)), storage);
    }

    @Override
    public boolean exist() {
        return folder.exists();
    }

    @Override
    public String getName() {
        return this.folder.getName();
    }

    @Override
    public IStorage[] list() {
        try{
            File[] files = this.folder.listFiles();
            IStorage[] ret = new IStorage[files.length];
            for(int i = 0; i < files.length; i++){
                if(files[i].isDirectory()){
                    ret[i] = getFolder(files[i].getName());
                }
                else{
                    ret[i] = getFile(files[i].getName());
                }
                
            }
            return ret;
        }
        catch(Exception e){
            return new IStorage[]{};
        }
    }
    
    @Override
    public long lastModified() {
    	return folder.lastModified();
    }
}
