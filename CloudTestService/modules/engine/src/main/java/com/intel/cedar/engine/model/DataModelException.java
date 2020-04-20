package com.intel.cedar.engine.model;

public class DataModelException extends Exception {
    public DataModelException() {
        super();
    }

    public DataModelException(String message) {
        super(message);
    }

    public DataModelException(String message, Throwable e) {
        super(message, e);
    }

    public DataModelException(Throwable e) {
        super(e);
    }
}
