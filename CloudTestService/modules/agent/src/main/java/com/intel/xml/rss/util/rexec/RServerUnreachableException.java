package com.intel.xml.rss.util.rexec;

/**
 * This code should compilable under java source version 1.4.
 * 
 * @author han
 * 
 */
public class RServerUnreachableException extends RException {

    private static final long serialVersionUID = -5761304111920141447L;

    public RServerUnreachableException(String rserver, String message,
            Throwable cause) {
        super(rserver, message, cause);
    }

}
