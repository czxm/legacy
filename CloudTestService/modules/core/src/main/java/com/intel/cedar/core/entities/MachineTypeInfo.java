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

import com.intel.cedar.util.EntityUtil;
import com.intel.cedar.util.EntityWrapper;

@Entity
@PersistenceContext(name = "cedar_general")
@Table(name = "machineTypes")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class MachineTypeInfo implements Comparable<MachineTypeInfo> {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id = -1l;
    @Column(name = "cloudId")
    private Long cloudId; // valid only if it's used be a cloud
    @Column(name = "type")
    private String type;
    @Column(name = "cpu")
    private Integer cpu; // cpu numbers
    @Column(name = "memory")
    private Integer memory; // memory size
    @Column(name = "disk")
    private Integer disk; // disk size
    @Column(name = "secondDisk")
    private Integer secondDisk; // secondary disk size aka Ephemeral
    @Column(name = "maxInstances")
    private Integer max; // maximum supported instances
    @Column(name = "free")
    private Integer free; // running instances of this type
    @Column(name = "enabled")
    private Boolean enabled;

    public static MachineTypeInfo load(Long id) {
        EntityWrapper<MachineTypeInfo> db = EntityUtil
                .getMachineTypeEntityWrapper();
        try {
            return db.load(MachineTypeInfo.class, id);
        } finally {
            db.rollback();
        }
    }

    public MachineTypeInfo() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCloudId() {
        return cloudId;
    }

    public void setCloudId(Long cloudId) {
        this.cloudId = cloudId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getCpu() {
        return cpu;
    }

    public void setCpu(Integer cpu) {
        this.cpu = cpu;
    }

    public Integer getMemory() {
        return memory;
    }

    public void setMemory(Integer memory) {
        this.memory = memory;
    }

    public Integer getDisk() {
        return disk;
    }

    public void setDisk(Integer disk) {
        this.disk = disk;
    }

    public Integer getSecondDisk() {
        return secondDisk != null ? secondDisk : 0;
    }

    public void setSecondDisk(Integer disk) {
        this.secondDisk = disk;
    }

    public CloudInfo getCloudInfo() {
        for (CloudInfo cloud : EntityUtil.listClouds()) {
            if (cloud.getId().equals(cloudId)) {
                return cloud;
            }
        }

        return null;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    public Integer getFree() {
        return free;
    }

    public void setFree(Integer free) {
        this.free = free;
    }

    @Override
    public int compareTo(MachineTypeInfo o) {
        int n = this.getCpu() - o.getCpu();
        if (n != 0)
            return n;
        else {
            n = this.getMemory() - o.getMemory();
            if (n != 0)
                return n;
            else {
                return this.getDisk() - o.getDisk();
            }
        }
    }
}
