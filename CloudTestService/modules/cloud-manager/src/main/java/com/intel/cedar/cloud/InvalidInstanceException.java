package com.intel.cedar.cloud;

public class InvalidInstanceException extends CloudException {
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    public InvalidInstanceException() {

    }

    public InvalidInstanceException(String errorMsg) {
        super(errorMsg);
    }
}
