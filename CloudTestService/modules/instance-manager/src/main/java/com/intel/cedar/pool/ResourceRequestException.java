package com.intel.cedar.pool;

import java.io.Serializable;

import com.intel.cedar.core.CedarException;

public class ResourceRequestException extends CedarException implements
        Serializable {
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    public ResourceRequestException() {
        this("Resource request failed");
    }

    public ResourceRequestException(String errorMsg) {
        super(errorMsg);
    }

    public ResourceRequestException(String errorMsg, Throwable t) {
        super(errorMsg, t);
    }
}
