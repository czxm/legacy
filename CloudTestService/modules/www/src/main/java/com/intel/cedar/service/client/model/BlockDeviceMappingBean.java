package com.intel.cedar.service.client.model;

import java.io.Serializable;

import com.extjs.gxt.ui.client.data.BaseModel;

public class BlockDeviceMappingBean extends BaseModel implements Serializable {
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    private String virtualName;
    private String deviceName;
    private String snapshotId;
    private int volumeSize; // GB
    private boolean deleteOnTerminate;

    public BlockDeviceMappingBean() {

    }

    public BlockDeviceMappingBean(String virtualName, String deviceName) {
        this.virtualName = virtualName;
        this.deviceName = deviceName;
    }

    public BlockDeviceMappingBean(String deviceName, String snapshotId,
            int volumeSize, boolean deleteOnTerminate) {
        this.deviceName = deviceName;
        this.snapshotId = snapshotId;
        this.volumeSize = volumeSize;
        this.deleteOnTerminate = deleteOnTerminate;
    }

    public String getVirtualName() {
        return virtualName;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getSnapshotId() {
        return snapshotId;
    }

    public int getVolumeSize() {
        return volumeSize;
    }

    public boolean isDeleteOnTerminate() {
        return deleteOnTerminate;
    }

    public String toString() {
        return "BlockDeviceMappingBean[virtualName=" + virtualName
                + ", deviceName=" + deviceName + ", snapshotId=" + snapshotId
                + ", volumeSize=" + volumeSize + ", delOnTerm="
                + deleteOnTerminate + "]";
    }
}
