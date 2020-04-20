package com.intel.xml.rss.util;

/**
 * A struct like class to hold the execution result.
 * <p/>
 * Author: hshen5
 */
public class CmdExecutionResult {

    /**
     * Log for stdout, or for stdout and stderr combined, which could be set in
     * the corresponding executor.
     */
    public String log;

    /**
     * Log for stderr.
     */
    public String log2;

    /**
     * As is.
     */
    public int exitValue;

}
