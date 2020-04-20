package com.intel.cedar.service.client.model;

public class VolumeInfoBean extends CedarBaseModel {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    private Long id = -1L;
    private String name;
    private String path;
    private Integer size; // unit is GB
    private String volumeId;
    private Long cloudId = -1L; // valid only associate by cloud
    private Long instanceId = -1L; // valid only attached to instance
    private String cloudName; // valid only associate with cloud
    private Integer deviceIndex;
    private Integer attachedCount;
    private Boolean pooled;
    private String comment;
    private UserInfoBean user;

    public VolumeInfoBean() {

    }

    @Override
    public void refresh() {
        set("Id", id);
        set("Name", name);
        set("Path", path);
        set("Size", size);
        set("VolumeId", volumeId);
        set("cloudId", cloudId);
        set("InstanceId", instanceId);
        set("CloudName", cloudName);
        set("deviceIndex", deviceIndex);
        set("attachedCount", attachedCount);
        set("Comment", comment);
        set("User", user.getUserName());
    }

    public void setDeviceIndex(Integer deviceIndex) {
        this.deviceIndex = deviceIndex;
    }

    public Integer getDeviceIndex() {
        return deviceIndex;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setCloudId(Long cloudId) {
        this.cloudId = cloudId;
    }

    public Long getCloudId() {
        return cloudId;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getSize() {
        return size;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setAttachedCount(Integer attachedCount) {
        this.attachedCount = attachedCount;
    }

    public Integer getAttachedCount() {
        return attachedCount;
    }

    public void setCloudName(String cloudName) {
        this.cloudName = cloudName;
    }

    public String getCloudName() {
        return cloudName;
    }

    public void setVolumeId(String volumeId) {
        this.volumeId = volumeId;
    }

    public String getVolumeId() {
        return volumeId;
    }

    @Override
    public boolean equals(Object obj) {
        VolumeInfoBean f;
        if (!(obj instanceof VolumeInfoBean))
            return false;
        f = (VolumeInfoBean) obj;
        if (id.equals(f.id) && cloudId.equals(f.cloudId)
                && volumeId.equals(f.getVolumeId()))
            return true;
        return false;
    }

    public void setPooled(Boolean pooled) {
        this.pooled = pooled;
    }

    public Boolean getPooled() {
        return pooled;
    }

    public UserInfoBean getUser() {
        return user;
    }

    public void setUser(UserInfoBean user) {
        this.user = user;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

}
