package com.intel.xml.rss.util.rexec;

/**
 * This code should compilable under java source version 1.4.
 * 
 * @author han
 * 
 */
public class RJobTimedOutException extends RException {

    /**
   * 
   */
    private static final long serialVersionUID = 5880293540589680877L;

    public RJobTimedOutException(String rserver) {
        super(rserver, "Job timed out");
    }

}
