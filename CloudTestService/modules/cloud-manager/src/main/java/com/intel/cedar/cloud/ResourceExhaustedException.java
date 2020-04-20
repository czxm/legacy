package com.intel.cedar.cloud;

public class ResourceExhaustedException extends CloudException {
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    public ResourceExhaustedException() {
        super("Resource exhausted!");
    }

    public ResourceExhaustedException(String errorMsg) {
        super(errorMsg);
    }
}
