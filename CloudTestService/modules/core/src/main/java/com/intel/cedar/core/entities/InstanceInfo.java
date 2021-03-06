/**
 * 
 */
package com.intel.cedar.core.entities;

import java.util.List;
import java.util.Properties;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.PersistenceContext;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.cedar.util.EntityUtil;
import com.intel.cedar.util.EntityWrapper;
import com.intel.xml.rss.util.DateTimeRoutine;

@Entity
@PersistenceContext(name = "cedar_general")
@Table(name = "instances")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class InstanceInfo extends AbstractHostInfo implements
        Comparable<InstanceInfo> {

    private final static Logger LOG = LoggerFactory
            .getLogger(InstanceInfo.class);

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id = -1l;
    @Column(name = "cloudId")
    private Long cloudId; // only valid if managed by cloud
    @Column(name = "machineId")
    private Long machineId; // refer to the machine's ID
    @Column(name = "instanceId")
    private String instanceId; // generated by cloud
    @Column(name = "typeId")
    private Long typeId; // machine type
    @Column(name = "keyname")
    private String keyname; // keyname
    @Column(name = "privateIp")
    private String privateIp; // private IP address
    @Column(name = "privateDns")
    private String privateDns; // private DNS
    @Column(name = "creationTime")
    private Long creationTime; // when is this instance started
    @Column(name = "remoteDisplay")
    private String remoteDisplay; // the URL for this instance's remote display
    @Column(name = "standby")
    private Boolean standby;
    @Column(name = "associated")
    private Boolean associated;

    public static InstanceInfo load(Long id) {
        EntityWrapper<InstanceInfo> db = EntityUtil.getInstanceEntityWrapper();
        try {
            return db.load(InstanceInfo.class, id);
        } finally {
            db.rollback();
        }
    }

    public InstanceInfo() {
    }

    public String getPrivateIp() {
        return privateIp == null ? "" : privateIp;
    }

    public void setPrivateIp(String ip) {
        this.privateIp = ip;
    }

    public String getPrivateDns() {
        return privateDns == null ? "" : privateDns;
    }

    public void setPrivateDns(String dns) {
        this.privateDns = dns;
    }

    public Long getMachineId() {
        return machineId;
    }

    public void setMachineId(Long machineId) {
        this.machineId = machineId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
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

    public MachineInfo.OS getOs() {
        return getMachineInfo().getOs();
    }

    public MachineInfo.ARCH getArch() {
        return getMachineInfo().getArch();
    }

    public MachineInfo getMachineInfo() {
        EntityWrapper<MachineInfo> ms = EntityUtil.getMachineEntityWrapper();
        try {
            MachineInfo mi = new MachineInfo();
            mi.setCloudId(cloudId);
            for (MachineInfo m : ms.query(mi)) {
                if (m.getId().equals(machineId))
                    return m;
            }
        } finally {
            ms.rollback();
        }
        return null;
    }

    public CloudInfo getCloudInfo() {
        for (CloudInfo cloud : EntityUtil.listClouds()) {
            if (cloud.getId().equals(cloudId))
                return cloud;
        }
        return null;
    }

    public MachineTypeInfo getMachineTypeInfo() {
        EntityWrapper<MachineTypeInfo> ms = new EntityWrapper<MachineTypeInfo>();
        try {
            return ms.load(MachineTypeInfo.class, typeId);
        } finally {
            ms.rollback();
        }
    }

    public String getKeyName() {
        return keyname;
    }

    public void setKeyName(String keyname) {
        this.keyname = keyname;
    }

    public synchronized void saveChanges() {
        EntityWrapper<InstanceInfo> db = EntityUtil.getInstanceEntityWrapper();
        try {
            InstanceInfo change = db.load(InstanceInfo.class, id);
            if (change == null){
                db.rollback();
                return;
            }
            change.setCloudId(cloudId);
            change.setHost(host);
            change.setGatewayId(gatewayId);
            change.setInstanceId(instanceId);
            change.setKeyName(keyname);
            change.setMachineId(machineId);
            change.setState(getState());
            change.setTypeId(typeId);
            change.setRetryCount(getRetryCount());
            change.setNotifyCount(getNotifyCount());
            change.setProxyAuth(getProxyAuth());
            change.setProxyHost(getProxyHost());
            change.setProxyPasswd(getProxyPasswd());
            change.setProxyPort(getProxyPort());
            change.setPooled(getPooled());
            change.setUserId(getUserId());
            change.setRemoteDisplay(getRemoteDisplay());
            change.setStandy(standby);
            change.setAssociated(associated);
            db.merge(change);
            db.commit();
        } catch (Exception e) {
            LOG.info("failed to save InstanceInfo:", e);
            db.rollback();
        }
    }

    public void refresh() {
        EntityWrapper<InstanceInfo> db = EntityUtil
                .getInstanceEntityWrapper();
        try {
            InstanceInfo change = db.load(InstanceInfo.class, id);
            if(change == null)
                return;
            host = change.getHost();
            this.cloudId = change.getCloudId();
            this.typeId = change.getTypeId();
            this.machineId = change.getMachineId();
            this.instanceId = change.getInstanceId();
            this.keyname = change.getKeyName();
            this.setProxyAuth(change.getProxyAuth());
            this.setProxyHost(change.getProxyHost());
            this.setProxyPasswd(change.getProxyPasswd());
            this.setProxyPort(change.getProxyPort());
            this.setPooled(change.getPooled());
            this.setUserId(change.getUserId());
            this.setGatewayId(change.getGatewayId());
            this.setRetryCount(change.getRetryCount());
            this.setNotifyCount(change.getNotifyCount());
            this.setState(change.getState());
            this.setRemoteDisplay(change.getRemoteDisplay());
            this.setStandy(change.getStandby());
            this.setAssociated(change.getAssociated());
        } catch (Exception e) {
            LOG.info("failed to refresh InstanceInfo:", e);
        } finally{
            db.rollback();
        }
    }
    
    public int compareTo(InstanceInfo o) {
        return this.getInstanceId().compareTo(o.getInstanceId());
    }

    public Long getCreationTime() {
        if (creationTime == null) {
            creationTime = System.currentTimeMillis();
        }
        return creationTime;
    }

    public void setCreationTime(Long creationTime) {
        this.creationTime = creationTime;
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

    @Override
    public List<String> getCapabilities() {
        return this.getMachineInfo().getCapabilities();
    }

    @Override
    public Properties getProperties() {
        return this.getMachineInfo().getProperties();
    }

    @Override
    public boolean isValid() {
        return load(getId()) != null;
    }

    @Override
    public String getRootPath() {
        if (this.getMachineInfo().getOs().isWindows()) {
            return "C:";
        } else {
            return "";
        }
    }

    // only used when importing instances from newly registered cloud
    public void setPortMappings(List<NATInfo> n) {
        EntityWrapper<NATInfo> db = EntityUtil.getNATEntityWrapper();
        try {
            for (NATInfo i : n) {
                db.add(i);
            }
        } finally {
            db.commit();
        }
    }

    public List<NATInfo> getPortMappings() {
        EntityWrapper<NATInfo> db = EntityUtil.getNATEntityWrapper();
        try {
            NATInfo n = new NATInfo();
            n.setInstanceId(getId());
            return db.query(n);
        } finally {
            db.rollback();
        }
    }

    public void assignPortMapping(NATInfo i) {
        if (gatewayId == null)
            return;
        if (i.getInstanceId() == null)
            i.setInstanceId(getId());
        if (i.getGatewayId() == null) {
            i.setGatewayId(gatewayId);
        }
        EntityWrapper<NATInfo> db = EntityUtil.getNATEntityWrapper();
        db.add(i);
        db.commit();
    }

    public String getRemoteDisplay() {
        return remoteDisplay;
    }

    public void setRemoteDisplay(String remoteDisplay) {
        this.remoteDisplay = remoteDisplay;
    }
    
    public Boolean getStandby() {
        if (standby == null)
            return false;
        return standby;
    }

    public void setStandy(Boolean standby) {
        this.standby = standby;
    }
    
    public Boolean getAssociated() {
        if (associated == null)
            return false;
        return associated;
    }

    public void setAssociated(Boolean associated) {
        this.associated = associated;
    }
}
