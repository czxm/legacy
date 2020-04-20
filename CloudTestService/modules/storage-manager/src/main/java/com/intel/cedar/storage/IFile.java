package com.intel.cedar.storage;

import java.io.InputStream;

public interface IFile extends IStorage {
    public InputStream getContents() throws Exception;

    public void setContents(InputStream source) throws Exception;
    
    public long length();
}
