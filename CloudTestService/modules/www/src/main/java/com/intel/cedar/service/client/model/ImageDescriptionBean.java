package com.intel.cedar.service.client.model;

import java.io.Serializable;
import java.util.ArrayList;

import com.extjs.gxt.ui.client.data.BaseModel;

public class ImageDescriptionBean extends BaseModel implements Serializable {
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    private String imageId;
    private String imageLocation;
    private String imageOwnerId;
    private String imageState;
    private boolean isPublic;
    private ArrayList<String> productCodes;
    private String architecture;
    private String imageType;
    private String kernelId;
    private String ramdiskId;
    private String platform;

    private String reason;
    private String imageOwnerAlias;
    private String name;
    private String description;
    private String rootDeviceType;
    private String rootDeviceName;
    private ArrayList<BlockDeviceMappingBean> blockDeviceMapping;
    private String virtualizationType;

    public ImageDescriptionBean() {

    }

    public ImageDescriptionBean(String id, String loc, String owner,
            String state, Boolean isPublic, ArrayList<String> productCodes,
            String architecture, String imageType, String kernelId,
            String ramdiskId, String platform, String reason,
            String imageOwnerAlias, String name, String description,
            String rootDeviceType, String rootDeviceName,
            ArrayList<BlockDeviceMappingBean> blockDeviceMapping,
            String virtualizationType) {
        this.imageId = id;
        set("ImageId", imageId);
        this.imageLocation = loc;
        set("ImageLocation", imageLocation);
        this.imageOwnerId = owner;
        this.imageState = state;
        this.isPublic = isPublic;
        this.productCodes = productCodes;
        this.architecture = architecture;
        set("Architecture", architecture);
        this.imageType = imageType;
        set("ImageType", imageType);
        this.kernelId = kernelId;
        set("KernelId", kernelId);
        this.ramdiskId = ramdiskId;
        set("RamdiskId", ramdiskId);
        this.platform = platform;
        set("Platform", platform);
        this.reason = reason;
        this.imageOwnerAlias = imageOwnerAlias;
        this.name = name;
        this.description = description;
        this.rootDeviceType = rootDeviceType;
        this.rootDeviceName = rootDeviceName;
        this.blockDeviceMapping = blockDeviceMapping;
        this.virtualizationType = virtualizationType;
    }

    public String getImageId() {
        return imageId;
    }

    public String getImageLocation() {
        return imageLocation;
    }

    public String getImageOwnerId() {
        return imageOwnerId;
    }

    public String getImageState() {
        return imageState;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public ArrayList<String> getProductCodes() {
        return productCodes;
    }

    public String getArchitecture() {
        return architecture;
    }

    public String getImageType() {
        return imageType;
    }

    public String getKernelId() {
        return kernelId;
    }

    public String getRamdiskId() {
        return ramdiskId;
    }

    public String getPlatform() {
        return platform;
    }

    public String getReason() {
        return reason;
    }

    public String getImageOwnerAlias() {
        return imageOwnerAlias;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getRootDeviceType() {
        return rootDeviceType;
    }

    public String getRootDeviceName() {
        return rootDeviceName;
    }

    public ArrayList<BlockDeviceMappingBean> getBlockDeviceMapping() {
        return blockDeviceMapping;
    }

    public String getVirtualizationType() {
        return virtualizationType;
    }

    public String toString() {
        return "ImageDescriptionBean[ID=" + imageId + ", Loc=" + imageLocation
                + ", own=" + imageOwnerId + ", state=" + imageState
                + " isPublic=" + isPublic + ", arch=" + architecture
                + ", imgTyp=" + imageType + ", kernelId=" + kernelId
                + ", ramdiskId=" + ramdiskId + ", platform=" + platform
                + ", reason=" + reason + ", imgOwnrAlias=" + imageOwnerAlias
                + ", name=" + name + ", descrip=" + description
                + ", rootDevType=" + rootDeviceType + ", rootDevName="
                + rootDeviceName + "]";
    }
}
