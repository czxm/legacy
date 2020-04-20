package com.intel.xml.rss.util.rexec;

/**
 * This code should compilable under java source version 1.4.
 * 
 * @author han
 * 
 */
public abstract class RException extends Exception {

    private String rserver;

    public RException(String rserver, String message) {
        super(message);
        this.rserver = rserver;
    }

    public RException(String rserver, String message, Throwable cause) {
        super(message, cause);
        this.rserver = rserver;
    }

    public String getRserver() {
        return rserver;
    }

    public void setRserver(String rserver) {
        this.rserver = rserver;
    }

}
