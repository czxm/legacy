/**
 * 
 */
package com.intel.cedar.core.entities;

import java.util.HashMap;

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

import com.intel.cedar.util.EntityUtil;
import com.intel.cedar.util.EntityWrapper;

@Entity
@PersistenceContext(name = "cedar_general")
@Table(name = "nat")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class NATInfo {
    private static Logger LOG = LoggerFactory.getLogger(NATInfo.class);
    private static HashMap<Integer, String> nameMap;
    static {
        nameMap = new HashMap<Integer, String>();
        nameMap.put(8443, "HTTPS");
        nameMap.put(80, "HTTP");
        nameMap.put(22, "SSH");
        nameMap.put(3389, "RDP");
        nameMap.put(5901, "VNC");
        nameMap.put(10614, "CedarAgent");
    }

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id = -1l;
    @Column(name = "instanceId")
    private Long instanceId; // refer to the virtual machine's ID
    @Column(name = "nodeId")
    private Long nodeId; // refer to the physical machine's ID
    @Column(name = "cloudNodeId")
    private Long cloudNodeId; // refer to the CloudNode machine's ID
    @Column(name = "gatewayId")
    private Long gatewayId; // refer to gateway
    @Column(name = "name")
    private String name; // e.g. VNC / RDP
    @Column(name = "port")
    private Integer port; // opened port of the instance
    @Column(name = "mappedPort")
    private Integer mappedPort; // mapped port to the instance
    @Column(name = "refCount")
    private Integer refCount;

    public static NATInfo load(Long id) {
        EntityWrapper<NATInfo> db = EntityUtil.getNATEntityWrapper();
        try {
            return db.load(NATInfo.class, id);
        } finally {
            db.rollback();
        }
    }

    public NATInfo() {
    }

    public NATInfo(String name, Integer port) {
        this.name = name;
        this.port = port;
    }

    public NATInfo(AbstractHostInfo i, Integer port) {
        if(i instanceof InstanceInfo)
            instanceId = i.getId();
        else if(i instanceof PhysicalNodeInfo)
            nodeId = i.getId();
        else if(i instanceof CloudNodeInfo)
            cloudNodeId = i.getId();
        this.port = port;
        this.name = nameMap.get(port);
        if (this.name == null) {
            this.name = "";
        }
    }

    public Long getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(Long gatewayId) {
        this.gatewayId = gatewayId;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getMappedPort() {
        if (mappedPort == null)
            return 0;
        return mappedPort;
    }

    public void setMappedPort(Integer mappedPort) {
        this.mappedPort = mappedPort;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCloudNodeId() {
        return cloudNodeId;
    }

    public void setCloudNodeId(Long cloudNodeId) {
        this.cloudNodeId = cloudNodeId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void saveChanges() {
        EntityWrapper<NATInfo> db = EntityUtil.getNATEntityWrapper();
        try {
            NATInfo change = db.load(NATInfo.class, id);
            if(change == null){
                db.rollback();
                return;
            }
            change.setGatewayId(gatewayId);
            change.setInstanceId(instanceId);
            change.setMappedPort(mappedPort);
            change.setName(name);
            change.setPort(port);
            change.setRefCount(getRefCount());
            db.merge(change);
            db.commit();
        } catch (Exception e) {
            LOG.info("failed to save NATInfo:", e);
            db.rollback();
        }
    }

    public Integer getRefCount() {
        if (refCount == null)
            return 0;
        return refCount;
    }

    public void setRefCount(Integer refCount) {
        this.refCount = refCount;
    }

    public static NATInfo[] getDefaultPort(String osName) {
        if (osName == null)
            return null;
        MachineInfo.OS os = MachineInfo.OS.fromString(osName);
        if (os.isWindows()) {
            return new NATInfo[] { new NATInfo("RDP", 3389),
                    new NATInfo("HTTP", 80), new NATInfo("HTTPS", 8443), };
        } else {
            return new NATInfo[] { new NATInfo("VNC", 5901),
                    new NATInfo("SSH", 22), new NATInfo("HTTP", 80),
                    new NATInfo("HTTPS", 8443), };
        }
    }
    
    public void setHost(AbstractHostInfo i) {
        if(i instanceof InstanceInfo)
            instanceId = i.getId();
        else if(i instanceof PhysicalNodeInfo)
            nodeId = i.getId();
        else if(i instanceof CloudNodeInfo)
            cloudNodeId = i.getId();
    }
}
