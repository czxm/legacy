package com.intel.cedar.storage;

import java.net.URI;

public interface IFolder extends IStorage {
    public IFile getFile(String name);

    public IFolder getFolder(String name);

    public IFile getFile(URI uri);

    public IFolder getFolder(URI uri);
    
    public IStorage[] list();
}
