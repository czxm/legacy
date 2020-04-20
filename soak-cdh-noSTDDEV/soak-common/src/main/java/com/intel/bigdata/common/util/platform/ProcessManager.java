package com.intel.bigdata.common.util.platform;

import java.io.IOException;

/**
 * This class arranges to perform service related operations.
 */
public interface ProcessManager {

    public void restart(String name) throws IOException;

    public void start(String name) throws IOException;

    public void stop(String name) throws IOException;

    public String status(String name) throws IOException;
}