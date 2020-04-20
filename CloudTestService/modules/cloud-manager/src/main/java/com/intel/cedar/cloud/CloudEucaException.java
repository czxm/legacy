package com.intel.cedar.cloud;

public class CloudEucaException extends CloudException {
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    public CloudEucaException() {

    }

    public CloudEucaException(String errorMsg) {
        super(errorMsg);
    }

    public CloudEucaException(String errorMsg, Throwable t) {
        super(errorMsg, t);
    }
}
