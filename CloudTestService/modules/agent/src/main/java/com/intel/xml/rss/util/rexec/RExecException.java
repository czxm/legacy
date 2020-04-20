package com.intel.xml.rss.util.rexec;

public class RExecException extends RException {

    private static final long serialVersionUID = -1357667000426159046L;

    public RExecException(String rserver, String message, Throwable cause) {
        super(rserver, message, cause);
    }

}
