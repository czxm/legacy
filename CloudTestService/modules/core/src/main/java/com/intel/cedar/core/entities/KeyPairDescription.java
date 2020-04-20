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

@Entity
@PersistenceContext(name = "cedar_general")
@Table(name = "keypairs")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class KeyPairDescription {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id = -1l;
    @Column(name = "cloudId")
    private Long cloudId;
    @Column(name = "keyName")
    private String keyName;
    @Column(name = "keyFingerPrint")
    private String keyFingerPrint;

    public KeyPairDescription() {

    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setCloudId(Long cloudId) {
        this.cloudId = cloudId;
    }

    public Long getCloudId() {
        return cloudId;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyFingerPrint(String keyFingerPrint) {
        this.keyFingerPrint = keyFingerPrint;
    }

    public String getKeyFingerPrint() {
        return keyFingerPrint;
    }

    public CloudInfo getCloudInfo() {
        for (CloudInfo info : EntityUtil.listClouds()) {
            if (info.getId().equals(cloudId))
                return info;
        }

        return null;
    }

    public String toString() {
        return "KeyPairInfo [ name = " + keyName + ", fingerPrint = "
                + keyFingerPrint + " ]";
    }
}
