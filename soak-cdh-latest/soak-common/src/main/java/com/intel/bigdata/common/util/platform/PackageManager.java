package com.intel.bigdata.common.util.platform;

import java.io.IOException;

/**
 * This interface defines package related operations.
 */
public interface PackageManager {

    public void install(String name) throws IOException;

    public void uninstall(String name) throws IOException;

    public void upgrade(String name) throws IOException;
}
