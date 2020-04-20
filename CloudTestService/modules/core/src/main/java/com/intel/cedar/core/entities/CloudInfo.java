package com.intel.cedar.core.entities;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.common.collect.Lists;
import com.intel.cedar.core.CedarException;
import com.intel.cedar.util.EntityUtil;
import com.intel.cedar.util.EntityWrapper;
import com.intel.cedar.util.protocal.ModelStream;

import edu.emory.mathcs.backport.java.util.Collections;

@Entity
@PersistenceContext(name = "cedar_general")
@Table(name = "clouds")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class CloudInfo {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id = -1l;
    @Column(name = "name")
    private String name;
    @Column(name = "host")
    private String host;
    @Column(name = "port")
    private Integer port;
    @Column(name = "secured")
    // HTTPS ?
    private Boolean secured;
    @Column(name = "protocol")
    private String protocol; // only EC2 supported for now
    @Column(name = "service")
    private String service;
    @Column(name = "enabled")
    private Boolean enabled;

    @Column(name = "param1")
    private String param1; // ACCESS_KEY for EC2
    @Column(name = "param2")
    private String param2; // SECRET_KEY for EC2
    @Column(name = "param3")
    private String param3;
    @Column(name = "param4")
    private String param4;
    @Column(name = "param5")
    private String param5;

    @Column(name = "proxyHost")
    private String proxyHost;
    @Column(name = "proxyPort")
    private String proxyPort;
    @Column(name = "proxyAuth")
    private String proxyAuth;
    @Column(name = "proxyPasswd")
    private String proxyPasswd;

    @Column(name = "seperated")
    // it's a private network, and need port mapping to access the instance
    private Boolean seperated;
    @Column(name = "gateways")
    private String gateways;

    @Column(name = "volumeDevice")
    private String volumeDevice;

    @Column(name = "updateDNS")
    private Boolean updateDNS;

    @Column(name = "nodeUser")
    private String nodeUser;

    @Column(name = "nodePassword")
    private String nodePassword;

    @Column(name = "poolRatio")
    private Float poolRatio;
    
    @Column(name = "notifyCount")
    protected Long notifyCount; // (email) notification count for this cloud
    
    public static CloudInfo load(Long id) {
        EntityWrapper<CloudInfo> db = EntityUtil.getCloudEntityWrapper();
        try {
            return db.load(CloudInfo.class, id);
        } finally {
            db.rollback();
        }
    }

    public CloudInfo() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(final Boolean enabled) {
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(final String protocol) {
        this.protocol = protocol;
    }

    public String getService() {
        return service;
    }

    public void setService(final String service) {
        this.service = service;
    }

    public Boolean getSeperated() {
        return this.seperated;
    }

    public void setSeperated(final Boolean seperated) {
        this.seperated = seperated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        CloudInfo that = (CloudInfo) o;

        if (!id.equals(that.id))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return this.protocol + ":" + this.host + ":" + this.port
                + this.getService();
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Boolean getSecured() {
        return secured;
    }

    public void setSecured(Boolean secured) {
        this.secured = secured;
    }

    public String getParam1() {
        return param1;
    }

    public void setParam1(String param1) {
        this.param1 = param1;
    }

    public String getParam2() {
        return param2;
    }

    public void setParam2(String param2) {
        this.param2 = param2;
    }

    public String getParam3() {
        return param3;
    }

    public void setParam3(String param3) {
        this.param3 = param3;
    }

    public String getParam4() {
        return param4;
    }

    public void setParam4(String param4) {
        this.param4 = param4;
    }

    public String getParam5() {
        return param5;
    }

    public void setParam5(String param5) {
        this.param5 = param5;
    }

    public List<MachineInfo> getMachines() {
        return EntityUtil.listMachines(this);
    }

    public List<MachineTypeInfo> getMachineTypes(final boolean ascending) {
        List<MachineTypeInfo> result = EntityUtil.listMachineTypes(this);
        Collections.sort(result, new Comparator<MachineTypeInfo>() {
            public int compare(MachineTypeInfo o1, MachineTypeInfo o2) {
                if (ascending) {
                    int n = o1.getCpu() - o2.getCpu();
                    if (n != 0)
                        return n;
                    else {
                        n = o1.getMemory() - o2.getMemory();
                        if (n != 0)
                            return n;
                        else {
                            return o1.getDisk() - o2.getDisk();
                        }
                    }
                } else {
                    int n = o2.getCpu() - o1.getCpu();
                    if (n != 0)
                        return n;
                    else {
                        n = o2.getMemory() - o1.getMemory();
                        if (n != 0)
                            return n;
                        else {
                            return o2.getDisk() - o1.getDisk();
                        }
                    }
                }
            }
        });
        return result;
    }

    public List<KeyPairDescription> getKeyPairs() {
        return EntityUtil.listKeyPairs(this);
    }

    public List<VolumeInfo> getVolumes() {
        return EntityUtil.listVolumes(this);
    }

    public List<InstanceInfo> getInstances(EntityWrapper<InstanceInfo> db) {
        return EntityUtil.listInstances(db, this);
    }

    public List<InstanceInfo> getInstances() {
        return EntityUtil.listInstances(this);
    }

    public Long findMachineId(String imageId) throws CedarException {
        EntityWrapper<MachineInfo> db = new EntityWrapper<MachineInfo>();
        try {
            MachineInfo m = new MachineInfo();
            m.setCloudId(getId());
            m.setImageId(imageId);
            return db.getUnique(m).getId();
        } finally {
            db.rollback();
        }
    }

    public Long findTypeId(String type) throws CedarException {
        EntityWrapper<MachineTypeInfo> db = new EntityWrapper<MachineTypeInfo>();
        try {
            MachineTypeInfo m = new MachineTypeInfo();
            m.setCloudId(getId());
            m.setType(type);
            return db.getUnique(m).getId();
        } finally {
            db.rollback();
        }
    }

    public void registerGateways(List<GatewayInfo> gw) {
        EntityWrapper<GatewayInfo> db = EntityUtil.getGatewayEntityWrapper();
        List<GatewayInfo> list = EntityUtil.listGateways();
        for (GatewayInfo g : gw) {
            boolean duplicated = false;
            for (GatewayInfo dg : list) {
                if (g.getHost().equals(dg.getHost())) {
                    duplicated = true;
                    break;
                }
            }
            if (!duplicated)
                db.add(g);
        }
        db.commit();
        List<Long> ids = new ArrayList<Long>();
        db = EntityUtil.getGatewayEntityWrapper();
        for (GatewayInfo g : gw) {
            GatewayInfo i = new GatewayInfo();
            i.setHost(g.getHost());
            List<GatewayInfo> r = db.query(i);
            if (r.size() == 1) {
                ids.add(r.get(0).getId());
            }
        }
        db.rollback();
        this.gateways = new ModelStream<List<Long>>().serialize(ids);
    }

    public List<GatewayInfo> getGateways() {
        List<GatewayInfo> result = Lists.newArrayList();
        if (gateways != null) {
            for (Long gwId : new ModelStream<List<Long>>().generate(gateways)) {
                result.add(GatewayInfo.load(gwId));
            }
        }
        return result;
    }

    public synchronized GatewayInfo findGateway() {
        int mappedPorts = Integer.MAX_VALUE;
        GatewayInfo candidate = null;
        for (GatewayInfo p : getGateways()) {
            if (p.getMappedPorts() < mappedPorts) {
                mappedPorts = p.getMappedPorts();
                candidate = p;
            }
        }
        return candidate;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public String getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(String proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getProxyAuth() {
        return proxyAuth;
    }

    public void setProxyAuth(String proxyAuth) {
        this.proxyAuth = proxyAuth;
    }

    public String getProxyPasswd() {
        return proxyPasswd;
    }

    public void setProxyPasswd(String proxyPasswd) {
        this.proxyPasswd = proxyPasswd;
    }

    public String getVolumeDevice() {
        return volumeDevice;
    }

    public void setVolumeDevice(String volumeDevice) {
        this.volumeDevice = volumeDevice;
    }

    public Boolean getUpdateDNS() {
        return updateDNS;
    }

    public void setUpdateDNS(Boolean updateDNS) {
        this.updateDNS = updateDNS;
    }

    public String getNodeUser() {
        return nodeUser;
    }

    public void setNodeUser(String nodeUser) {
        this.nodeUser = nodeUser;
    }

    public String getNodePassword() {
        return nodePassword;
    }

    public void setNodePassword(String nodePassword) {
        this.nodePassword = nodePassword;
    }
    
    public Long getNotifyCount() {
        if (notifyCount == null) {
            notifyCount = 0L;
        }
        return notifyCount;
    }

    public void setNotifyCount(Long notifyCount) {
        this.notifyCount = notifyCount;
    }
    
    
    public Float getPoolRatio() {
        return poolRatio == null ? 0.5f : poolRatio;
    }

    public void setPoolRatio(Float poolRatio) {
        this.poolRatio = poolRatio;
    }

    public void saveChanges() {
        EntityWrapper<CloudInfo> db = EntityUtil
                .getCloudEntityWrapper();
        try {
            CloudInfo change = db.load(CloudInfo.class, id);
            if(change == null){
                db.rollback();
                return;
            }
            change.setEnabled(getEnabled());
            change.setProxyAuth(getProxyAuth());
            change.setProxyHost(getProxyHost());
            change.setProxyPasswd(getProxyPasswd());
            change.setProxyPort(getProxyPort());
            change.setNotifyCount(getNotifyCount());
            change.setHost(getHost());
            change.setName(getName());
            change.setNodePassword(getNodePassword());
            change.setNodeUser(getNodeUser());
            change.setParam1(getParam1());
            change.setParam2(getParam2());
            change.setParam3(getParam3());
            change.setParam4(getParam4());
            change.setParam5(getParam5());
            change.setPort(getPort());
            change.setProtocol(getProtocol());
            change.setSecured(getSecured());
            change.setSeperated(getSeperated());
            change.setService(getService());
            change.setUpdateDNS(getUpdateDNS());
            change.setVolumeDevice(getVolumeDevice());
            db.commit();
        } catch (Exception e) {
            e.printStackTrace();
            db.rollback();
        }
    }
}