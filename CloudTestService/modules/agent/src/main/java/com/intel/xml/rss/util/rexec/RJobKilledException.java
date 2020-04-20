package com.intel.xml.rss.util.rexec;

/**
 * This code should compilable under java source version 1.4.
 * 
 * @author han
 * 
 */
public class RJobKilledException extends RException {

    private static final long serialVersionUID = 5834464229142613780L;

    public RJobKilledException(String rserver) {
        super(rserver, "Job was killed");
    }

}
