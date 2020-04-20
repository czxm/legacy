/**
 * 
 */
package com.intel.cedar.core.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.cedar.user.UserInfo;
import com.intel.cedar.user.util.UserUtil;
import com.intel.cedar.util.CedarConfiguration;
import com.intel.cedar.util.EntityUtil;
import com.intel.cedar.util.EntityWrapper;
import com.intel.xml.rss.util.DateTimeRoutine;

@Entity
@PersistenceContext(name = "cedar_general")
@Table(name = "volumes")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class VolumeInfo implements Comparable<VolumeInfo> {
    private final static Logger LOG = LoggerFactory.getLogger(VolumeInfo.class);

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;
    @Column(name = "name")
    private String name;
    @Column(name = "path")
    private String path; // folder name in the target instance
    @Column(name = "imageId")
    private String imageId;
    @Column(name = "cloudId")
    private Long cloudId; // valid only if it's a cloud managed volume
    @Column(name = "size")
    private Integer size; // unit is G
    @Column(name = "attachedInstanceId")
    private String instanceId; // cloud relative instance id of the attached
                               // instance
    @Column(name = "attachedInstance")
    private Long attached; // index id of the attached instance
    @Column(name = "deviceIndex")
    private Integer deviceIndex; // index of the attached volume
    @Column(name = "attachedCount")
    private Integer attachedCount; // how many times this volume attached.
    @Column(name = "pooled")
    private Boolean pooled; // indicate that it's managed by resource pool
    @Column(name = "held")
    private Boolean held; // indicate that this volume is held for reproduce
    @Column(name = "userId")
    private Long userId; // owner, default is admin
    @Column(name = "creationTime")
    private Long creationTime; // when is this volume created
    @Column(name = "attachTime")
    private Long attachTime; // when is this volume attached recently
    @Column(name = "comment")
    private String comment;

    public static VolumeInfo load(Long id) {
        EntityWrapper<VolumeInfo> db = EntityUtil.getVolumeEntityWrapper();
        try {
            return db.load(VolumeInfo.class, id);
        } finally {
            db.rollback();
        }
    }

    public VolumeInfo() {
    }

    public Long getAttached() {
        return attached;
    }

    public void setAttached(Long attached) {
        this.attached = attached;
        if (attached != null) {
            InstanceInfo i = InstanceInfo.load(attached);
            if (i != null) {
                this.instanceId = i.getInstanceId();
                return;
            }
        }
        this.instanceId = "";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public Long getCloudId() {
        return cloudId;
    }

    public void setCloudId(Long cloudId) {
        this.cloudId = cloudId;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CloudInfo getCloudInfo() {
        for (CloudInfo cloud : EntityUtil.listClouds()) {
            if (cloud.getId().equals(cloudId))
                return cloud;
        }
        return null;
    }

    public AbstractHostInfo getAttachedInstance() {
        if (attached == null)
            return null;
        if (isCloudVolume()) {
            return InstanceInfo.load(attached);
        } else {
            return PhysicalNodeInfo.load(attached);
        }
    }

    public String getAttachedInstanceId() {
        return this.instanceId;
    }

    public void setAttachedInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public Integer getDeviceIndex() {
        return deviceIndex;
    }

    public void setDeviceIndex(Integer deviceIndex) {
        this.deviceIndex = deviceIndex;
    }

    public String getDeviceName() {
        if (deviceIndex < 0)
            return "";
        try {
            String dev = this.getCloudInfo().getVolumeDevice();
            char c = dev.charAt(dev.length() - 1);
            String sdev = dev.substring(0, dev.length() - 1);
            return "/dev/" + sdev + (char) (c + deviceIndex);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public void saveChanges() {
        EntityWrapper<VolumeInfo> db = EntityUtil.getVolumeEntityWrapper();
        try {
            VolumeInfo change = db.load(VolumeInfo.class, id);
            if (change == null){
                db.rollback();
                return;
            }
            change.setCloudId(cloudId);
            change.setAttached(attached);
            change.setDeviceIndex(deviceIndex);
            change.setImageId(imageId);
            change.setName(name);
            change.setSize(size);
            change.setPooled(pooled);
            change.setAttachedInstanceId(instanceId);
            change.setUserId(userId);
            change.setHeld(held);
            change.setComment(getComment());
            change.setPath(getPath());
            change.setAttachedCount(getAttachedCount());
            change.setAttachTime(attachTime);
            db.merge(change);
            db.commit();
        } catch (Exception e) {
            LOG.info("failed to save VolumeInfo:", e);
            db.rollback();
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Integer getAttachedCount() {
        return attachedCount == null ? 0 : attachedCount;
    }

    public void setAttachedCount(Integer attachedCount) {
        this.attachedCount = attachedCount;
    }

    public void incAttachedCount() {
        if (attachedCount == null)
            attachedCount = 0;
        attachedCount++;
    }

    public boolean isCloudVolume() {
        return cloudId > 0;
    }

    public Boolean getPooled() {
        if (pooled == null)
            return false;
        return pooled;
    }

    public void setPooled(Boolean pooled) {
        this.pooled = pooled;
    }

    public UserInfo getOwner() {
        return UserUtil.getUserById(userId);
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Long creationTime) {
        this.creationTime = creationTime;
    }

    public Long getAttachTime() {
        return attachTime;
    }

    public void setAttachTime(Long attachTime) {
        this.attachTime = attachTime;
    }

    public String getCreationTimeString() {
        return DateTimeRoutine.millisToStdTimeString(creationTime);
    }

    public Long getLiveTime() {
        return System.currentTimeMillis() - creationTime;
    }

    public int getLiveDays() {
        return DateTimeRoutine.millisToDurationDays(getLiveTime());
    }

    public Boolean getHeld() {
        if (held == null)
            return false;
        return held;
    }

    public void setHeld(Boolean held) {
        this.held = held;
    }

    public boolean isValid() {
        return load(getId()) != null;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int compareTo(VolumeInfo o) {
        return this.getId().compareTo(o.getId());
    }
}
