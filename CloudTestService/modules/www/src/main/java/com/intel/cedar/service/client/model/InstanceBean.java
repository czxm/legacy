package com.intel.cedar.service.client.model;

import java.io.Serializable;

import com.extjs.gxt.ui.client.data.BaseModel;

public class InstanceBean extends BaseModel implements Serializable {

    public enum InstanceTypeBean {
        DEFAULT("m1.small"), LARGE("m1.large"), XLARGE("m1.xlarge"), MEDIUM_HCPU(
                "c1.medium"), XLARGE_HCPU("c1.xlarge"), XLARGE_HMEM("m2.xlarge"), XLARGE_DOUBLE_HMEM(
                "m2.2xlarge"), XLARGE_QUAD_HMEM("m2.4xlarge"), XLARGE_CLUSTER_COMPUTE(
                "cc1.4xlarge");

        private final String typeId;

        InstanceTypeBean(String typeId) {
            this.typeId = typeId;
        }

        public String getTypeId() {
            return typeId;
        }

        public static InstanceTypeBean getTypeFromString(String val) {
            for (InstanceTypeBean t : InstanceTypeBean.values()) {
                if (t.getTypeId().equals(val)) {
                    return t;
                }
            }
            return null;
        }
    }

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    private String _requestId;
    private String _owner;
    private String _resId;
    private String _requesterId;
    private String _imageId;
    private String _instanceId;
    private String _privateDnsName;
    private String _dnsName;
    private String _keyName;
    private String _state;
    private InstanceTypeBean _type;
    private String _launchTime; // yyyy-MM-dd HH:mm
    private String _availabilityZone;
    private String _kernelId;
    private String _ramdiskId;
    private String _platform;

    public InstanceBean() {

    }

    public InstanceBean(String requesterId, String owner, String resId,
            String requestId, String imageId, String instanceId,
            String privateDnsName, String dnsName, String keyName,
            String state, InstanceTypeBean type, String launchTime,
            String availabilityZone, String kernelId, String ramdiskId,
            String platform) {
        _requesterId = requesterId;
        set("RequestId", _requesterId);
        _owner = owner;
        set("Owner", _owner);
        _resId = resId;
        set("ReservationId", _resId);
        _requestId = requestId;
        set("RequestId", _requestId);
        _imageId = imageId;
        set("ImageId", _imageId);
        _instanceId = instanceId;
        set("InstanceId", _instanceId);
        _privateDnsName = privateDnsName;
        set("PrivateDnsName", _privateDnsName);
        _dnsName = dnsName;
        set("DnsName", _dnsName);
        _keyName = keyName;
        set("KeyName", _keyName);
        _state = state;
        set("State", _state);
        _type = type;
        set("Type", _type);
        _launchTime = launchTime;
        set("LaunchTime", _launchTime);
        _availabilityZone = availabilityZone;
        set("AvailabilityZone", _availabilityZone);
        _kernelId = kernelId;
        set("KernelId", _kernelId);
        _ramdiskId = ramdiskId;
        set("RamdiskId", _ramdiskId);
        _platform = platform;
        set("Platform", _platform);
    }

    public String getRequesterId() {
        return _requesterId;
    }

    public String getOwner() {
        return _owner;
    }

    public String getReservationId() {
        return _resId;
    }

    public String getRequestId() {
        return _requestId;
    }

    public String getImageId() {
        return _imageId;
    }

    public String getInstanceId() {
        return _instanceId;
    }

    public String getPrivateDnsName() {
        return _privateDnsName;
    }

    public String getDnsName() {
        return _dnsName;
    }

    public String getKeyName() {
        return _keyName;
    }

    public String getState() {
        return _state;
    }

    public InstanceTypeBean getType() {
        return _type;
    }

    public String getLaunchTime() {
        return _launchTime;
    }

    public String getAvailabilityZone() {
        return _availabilityZone;
    }

    public String getKernelId() {
        return _kernelId;
    }

    public String getRamdiskId() {
        return _ramdiskId;
    }

    public String getPlatform() {
        return _platform;
    }

}
