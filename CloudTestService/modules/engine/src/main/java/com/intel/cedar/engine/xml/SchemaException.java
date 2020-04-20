package com.intel.cedar.engine.xml;

public class SchemaException extends XMLException {
    public SchemaException() {
        super();
    }

    public SchemaException(String message) {
        super(message);
    }

    public SchemaException(String message, Throwable e) {
        super(message, e);
    }

    public SchemaException(Throwable e) {
        super(e);
    }
}
