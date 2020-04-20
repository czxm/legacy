package com.intel.cedar.service.client.model;

import java.io.Serializable;
import java.util.ArrayList;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.intel.cedar.service.client.model.InstanceBean.InstanceTypeBean;

public class ReservationDescriptionBean extends BaseModel implements
        Serializable {
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    private String _requestId;
    private String _owner;
    private String _resId;
    private String _requesterId;

    private ArrayList<InstanceBean> _instances = new ArrayList<InstanceBean>();

    public ReservationDescriptionBean() {

    }

    public ReservationDescriptionBean(String requestId, String owner,
            String resId, String requesterId) {
        _requestId = requestId;
        set("RequestId", _requestId);
        _owner = owner;
        set("Owner", _owner);
        _resId = resId;
        set("ResId", _resId);
        _requesterId = requesterId;
        set("RequesterId", _requesterId);
        set("Instances", _instances);
    }

    public InstanceBean addInstance(String requesterId, String owner,
            String resId, String requestId, String imageId, String instanceId,
            String privateDnsName, String dnsName, String keyName,
            String state, InstanceTypeBean type, String launchTime,
            String availabilityZone, String kernelId, String ramdiskId,
            String platform) {
        InstanceBean instance = new InstanceBean(requesterId, owner, resId,
                requestId, imageId, instanceId, privateDnsName, dnsName,
                keyName, state, type, launchTime, availabilityZone, kernelId,
                ramdiskId, platform);
        _instances.add(instance);
        return instance;
    }

    public void addInstance(InstanceBean instance) {
        _instances.add(instance);
    }

    public String getRequestId() {
        return _requestId;
    }

    public String getOwner() {
        return _owner;
    }

    public String getReservationId() {
        return _resId;
    }

    public String getRequesterId() {
        return _requesterId;
    }

    public ArrayList<InstanceBean> getInstances() {
        return _instances;
    }
}
