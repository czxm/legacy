package com.intel.cedar.cloud;

public class UnsupportedCloudException extends CloudException {
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    public UnsupportedCloudException() {

    }

    public UnsupportedCloudException(String errorMsg) {
        super(errorMsg);
    }
}
