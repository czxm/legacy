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

import com.google.common.collect.Lists;
import com.intel.cedar.core.entities.MachineInfo.ARCH;
import com.intel.cedar.core.entities.MachineInfo.OS;
import com.intel.cedar.util.EntityUtil;
import com.intel.cedar.util.EntityWrapper;
import com.intel.cedar.util.protocal.ModelStream;

@Entity
@PersistenceContext(name = "cedar_general")
@Table(name = "physical_nodes")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class PhysicalNodeInfo extends AbstractHostInfo implements
        Comparable<PhysicalNodeInfo> {
    private final static Logger LOG = LoggerFactory
            .getLogger(PhysicalNodeInfo.class);

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id = -1l;
    @Column(name = "cloudId")
    private Long cloudId;

    @Column(name = "nodeName")
    // the unique name for this physical node
    private String nodeName;
    @Column(name = "os")
    private OS os;
    @Column(name = "arch")
    private ARCH arch;
    @Column(name = "caps", length = 4096)
    private String caps;
    @Column(name = "props", length = 4096)
    private String props;
    @Column(name = "cpu")
    private Integer cpu; // cpu numbers
    @Column(name = "memory")
    private Integer memory; // memory size (G)
    @Column(name = "disk")
    private Integer disk; // disk size for all test tasks
    @Column(name = "rootpath")
    private String rootPath; // the root path for all test tasks
    // it must be an absolute path
    // e.g: /testRoot on Linux or E:\testRoot on Windows
    @Column(name = "managed")
    // indicate whether it should be managed by CloudTestService
    private Boolean managed;

    @Column(name = "shared")
    private Boolean shared; // indicate that this node could be used by pool for
                            // other users

    @Column(name = "comment")
    private String comment; // machine comment

    /**
	 * 
	 */
    public static PhysicalNodeInfo load(Long id) {
        EntityWrapper<PhysicalNodeInfo> db = EntityUtil
                .getPhysicalNodeEntityWrapper();
        try {
            return db.load(PhysicalNodeInfo.class, id);
        } finally {
            db.rollback();
        }
    }

    public PhysicalNodeInfo() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OS getOs() {
        return os;
    }

    public void setOs(OS os) {
        this.os = os;
    }

    public ARCH getArch() {
        return arch;
    }

    public void setArch(ARCH arch) {
        this.arch = arch;
    }

    public String getOSName() {
        if (os != null)
            return os.getOSName();
        else
            return "N/A";
    }

    public String getArchitecture() {
        if (arch != null)
            return arch.name();
        else
            return "N/A";
    }

    public List<String> getCapabilities() {
        if (caps == null)
            return Lists.newArrayList();
        else
            return new ModelStream<List<String>>().generate(caps);
    }

    public void setCapabilities(List<String> features) {
        this.caps = new ModelStream<List<String>>().serialize(features);
    }

    public Properties getProperties() {
        if (props == null)
            return new Properties();
        else
            return new ModelStream<Properties>().generate(props);
    }

    public void setProperties(Properties properties) {
        this.props = new ModelStream<Properties>().serialize(properties);
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

    public int compareTo(PhysicalNodeInfo o) {
        return this.getId().compareTo(o.getId());
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public Boolean getManaged() {
        if (managed == null)
            return false;
        return managed;
    }

    public void setManaged(Boolean managed) {
        this.managed = managed;
    }

    public Boolean getShared() {
        if (shared == null)
            return false;
        return shared;
    }

    public void setShared(Boolean shared) {
        this.shared = shared;
    }

    public void decreaseDiskSize(int size) {
        this.disk -= size;
        saveChanges();
    }

    public void increaseDiskSize(int size) {
        this.disk += size;
        saveChanges();
    }

    public void saveChanges() {
        EntityWrapper<PhysicalNodeInfo> db = EntityUtil
                .getPhysicalNodeEntityWrapper();
        try {
            PhysicalNodeInfo change = db.load(PhysicalNodeInfo.class, id);
            if(change == null){
                db.rollback();
                return;
            }
            change.setHost(host);
            change.setProxyAuth(getProxyAuth());
            change.setProxyHost(getProxyHost());
            change.setProxyPasswd(getProxyPasswd());
            change.setProxyPort(getProxyPort());
            change.setPooled(getPooled());
            change.setUserId(getUserId());
            change.setArch(getArch());
            change.setOs(getOs());
            change.setCapabilities(getCapabilities());
            change.setCpu(getCpu());
            change.setMemory(getMemory());
            change.setDisk(getDisk());
            change.setGatewayId(getGatewayId());
            change.setManaged(getManaged());
            change.setProperties(getProperties());
            change.setRootPath(getRootPath());
            change.setShared(getShared());
            change.setComment(getComment());
            change.setRetryCount(getRetryCount());
            change.setNotifyCount(getNotifyCount());
            change.setState(getState());
            db.merge(change);
            db.commit();
        } catch (Exception e) {
            LOG.info("failed to save PhysicalNodeInfo:", e);
            db.rollback();
        }
    }

    public void refresh() {
        EntityWrapper<PhysicalNodeInfo> db = EntityUtil
                .getPhysicalNodeEntityWrapper();
        try {
            PhysicalNodeInfo change = db.load(PhysicalNodeInfo.class, id);
            host = change.getHost();
            this.setProxyAuth(change.getProxyAuth());
            this.setProxyHost(change.getProxyHost());
            this.setProxyPasswd(change.getProxyPasswd());
            this.setProxyPort(change.getProxyPort());
            this.setPooled(change.getPooled());
            this.setUserId(change.getUserId());
            this.setArch(change.getArch());
            this.setOs(change.getOs());
            this.setCapabilities(change.getCapabilities());
            this.setCpu(change.getCpu());
            this.setMemory(change.getMemory());
            this.setDisk(change.getDisk());
            this.setGatewayId(change.getGatewayId());
            this.setManaged(change.getManaged());
            this.setProperties(change.getProperties());
            this.setRootPath(change.getRootPath());
            this.setShared(change.getShared());
            this.setComment(change.getComment());
            this.setRetryCount(change.getRetryCount());
            this.setNotifyCount(change.getNotifyCount());
            this.setState(change.getState());
        } catch (Exception e) {
            LOG.info("failed to refresh PhysicalNodeInfo:", e);
        } finally{
            db.rollback();
        }
    }
    
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    @Override
    public boolean isValid() {
        return load(getId()) != null;
    }

    @Override
    public boolean isValidHost() {
        return host != null && !host.equals("") && !host.equals("0.0.0.0");
    }

    public void setCloudId(Long cloudId) {
        this.cloudId = cloudId;
    }

    public Long getCloudId() {
        return cloudId;
    }

    public List<NATInfo> getPortMappings() {
        EntityWrapper<NATInfo> db = EntityUtil.getNATEntityWrapper();
        try {
            NATInfo n = new NATInfo();
            n.setNodeId(getId());
            return db.query(n);
        } finally {
            db.rollback();
        }
    }

    public void assignPortMapping(NATInfo i) {
        if (gatewayId == null)
            return;
        if (i.getNodeId() == null)
            i.setNodeId(getId());
        if (i.getGatewayId() == null) {
            i.setGatewayId(gatewayId);
        }
        EntityWrapper<NATInfo> db = EntityUtil.getNATEntityWrapper();
        db.add(i);
        db.commit();
    }
}
