package com.intel.cedar.core;

public class CedarException extends Exception {

    public CedarException() {
        super("Internal Error.");
    }

    public CedarException(String message) {
        super(message);
    }

    public CedarException(Throwable ex) {
        super("Internal Error.", ex);
    }

    public CedarException(String message, Throwable ex) {
        super(message, ex);
    }
}
