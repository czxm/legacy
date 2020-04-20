package com.intel.cedar.storage;

import java.net.URI;

public interface IStorage {
    public IFolder getRoot();

    public boolean create();

    public boolean delete();

    public URI getURI();

    public boolean exist();

    public String getName();
    
    public long lastModified();
}
