package com.intel.cedar.cloud;

public class VolumeCreationException extends CloudException {
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    public VolumeCreationException() {

    }

    public VolumeCreationException(String errorMsg) {
        super(errorMsg);
    }
}
