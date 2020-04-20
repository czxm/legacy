package com.intel.cedar.engine.xml;

public class XPathException extends RuntimeException {
    protected String errorCode;

    public XPathException() {
        super();
    }

    public XPathException(String message) {
        super(message);
    }

    public XPathException(String message, Throwable e) {
        super(message, e);
    }

    public XPathException(Throwable e) {
        super(e);
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
