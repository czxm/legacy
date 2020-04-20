package com.intel.bigdata.common.util.platform;

/**
 * The interface defines local file system operations.
 */
public interface LocalFS {

    public boolean makeDir(String path);

    public boolean removeDir(String path);

    public boolean makeFile(String path, String content);

    public boolean makeFile(String path, String content, boolean override);

    public String readFile(String path);

}
