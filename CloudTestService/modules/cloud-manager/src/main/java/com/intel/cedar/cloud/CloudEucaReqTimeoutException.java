package com.intel.cedar.cloud;

public class CloudEucaReqTimeoutException extends CloudEucaException {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    public CloudEucaReqTimeoutException() {

    }

    public CloudEucaReqTimeoutException(String errorMsg) {
        super(errorMsg);
    }

    public CloudEucaReqTimeoutException(String errorMsg, Throwable t) {
        super(errorMsg, t);
    }
}
