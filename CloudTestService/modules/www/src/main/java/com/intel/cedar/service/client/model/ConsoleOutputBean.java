package com.intel.cedar.service.client.model;

import java.io.Serializable;

import com.extjs.gxt.ui.client.data.BaseModelData;

public class ConsoleOutputBean extends BaseModelData implements Serializable {
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    private String _requestId;
    private String _instanceId;
    private String _timestamp;
    private String _output; // naked (i.e. not escaped, not BASE64)

    public ConsoleOutputBean(String requestId, String instanceId,
            String timestamp, String output) {
        this._requestId = requestId;
        set("RequestId", _requestId);
        this._instanceId = instanceId;
        set("InstanceId", _instanceId);
        this._timestamp = timestamp;
        set("Timestamp", _timestamp);
        this._output = output;
        set("Output", _output);
    }

    public String getRequestId() {
        return _requestId;
    }

    public String getInstanceId() {
        return _instanceId;
    }

    public String getTimestamp() {
        return _timestamp;
    }

    public String getOutput() {
        return _output;
    }

    public String toString() {
        return "ConsoleOutputBean[requestId=" + _requestId + ", instanceId="
                + _instanceId + ", timestamp=" + _timestamp + ", output="
                + _output + "]";
    }
}
