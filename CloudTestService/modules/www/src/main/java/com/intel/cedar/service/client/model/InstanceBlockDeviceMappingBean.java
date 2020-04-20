package com.intel.cedar.service.client.model;

import java.io.Serializable;

import com.extjs.gxt.ui.client.data.BaseModel;

public class InstanceBlockDeviceMappingBean extends BaseModel implements
        Serializable {
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    private String deviceName;
    private String volumeId;
    private String status;
    private String attachTime;
    private boolean deleteOnTerminate;

    public InstanceBlockDeviceMappingBean() {

    }

    public InstanceBlockDeviceMappingBean(String deviceName, String volumeId,
            String status, String attachTime, boolean deleteOnTerminate) {
        this.deviceName = deviceName;
        set("DeviceName", deviceName);
        this.volumeId = volumeId;
        set("VolumnId", volumeId);
        this.status = status;
        set("Status", status);
        this.attachTime = attachTime;
        set("AttachTime", attachTime);
        this.deleteOnTerminate = deleteOnTerminate;
        set("DeleteOnTerminate", deleteOnTerminate);
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getVolumeId() {
        return volumeId;
    }

    public String getStatus() {
        return status;
    }

    public String getAttachtime() {
        return attachTime;
    }

    public boolean isDeleteOnTerminate() {
        return deleteOnTerminate;
    }

    public String toString() {
        return "InstanceBlockDeviceMappingBean[deviceName=" + deviceName
                + ", volumeId=" + volumeId + ", status=" + status
                + ", attachTime=" + attachTime.toString() + ", delOnTerm="
                + deleteOnTerminate + "]";
    }
}
