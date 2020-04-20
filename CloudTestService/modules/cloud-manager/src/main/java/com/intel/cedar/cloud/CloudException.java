package com.intel.cedar.cloud;

import java.io.Serializable;

import com.intel.cedar.core.CedarException;

public class CloudException extends CedarException implements Serializable {
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    public CloudException() {

    }

    public CloudException(String errorMsg) {
        super(errorMsg);
    }

    public CloudException(String errorMsg, Throwable t) {
        super(errorMsg, t);
    }
}
