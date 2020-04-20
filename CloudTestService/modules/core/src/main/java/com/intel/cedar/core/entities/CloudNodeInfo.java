/**
 * 
 */
package com.intel.cedar.core.entities;

import java.net.URI;
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
@Table(name = "cloud_nodes")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class CloudNodeInfo extends AbstractHostInfo implements
        Comparable<CloudNodeInfo> {
    private final static Logger LOG = LoggerFactory
            .getLogger(CloudNodeInfo.class);

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id = -1l;
    @Column(name = "cloudId")
    private Long cloudId;

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
    @Column(name = "managed")
    // indicate whether it should be managed by CloudTestService
    private Boolean managed;
    @Column(name = "content")
    // Cloud specific contents
    private String content;

    /**
	 * 
	 */
    public static CloudNodeInfo load(Long id) {
        EntityWrapper<CloudNodeInfo> db = EntityUtil
                .getCloudNodeEntityWrapper();
        try {
            return db.load(CloudNodeInfo.class, id);
        } finally {
            db.rollback();
        }
    }

    public CloudNodeInfo() {
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

    public String getRootPath() {
        throw new RuntimeException("NOT SUPPORTED");
    }

    public int compareTo(CloudNodeInfo o) {
        return this.getId().compareTo(o.getId());
    }

    public Boolean getManaged() {
        if (managed == null)
            return false;
        return managed;
    }

    public void setManaged(Boolean managed) {
        this.managed = managed;
    }

    public void saveChanges() {
        EntityWrapper<CloudNodeInfo> db = EntityUtil
                .getCloudNodeEntityWrapper();
        try {
            CloudNodeInfo change = db.load(CloudNodeInfo.class, id);
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
            change.setRetryCount(getRetryCount());
            change.setNotifyCount(getNotifyCount());
            change.setContent(getContent());
            change.setState(getState());
            db.merge(change);
            db.commit();
        } catch (Exception e) {
            LOG.info("failed to save PhysicalNodeInfo:", e);
            db.rollback();
        }
    }

    @Override
    public boolean isValid() {
        return load(getId()) != null;
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
            n.setCloudNodeId(getId());
            return db.query(n);
        } finally {
            db.rollback();
        }
    }

    public void assignPortMapping(NATInfo i) {
        if (i.getNodeId() == null)
            i.setCloudNodeId(getId());
        if (i.getGatewayId() == null) {
            i.setGatewayId(gatewayId);
        }
        EntityWrapper<NATInfo> db = EntityUtil.getNATEntityWrapper();
        db.add(i);
        db.commit();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getInstanceDisplayPort(InstanceInfo instance) {
        URI uri = URI.create(instance.getRemoteDisplay());
        if (this.gatewayId == null)
            return uri.getPort();
        else {
            for (NATInfo n : this.getPortMappings()) {
                if (n.getName().equals("display" + (uri.getPort() - 5900))) {
                    if(n.getMappedPort() > 0){
                        return n.getMappedPort();
                    }
                }
            }
            GatewayInfo gw = GatewayInfo.load(getGatewayId());
            if (gw != null) {
                NATInfo nat = new NATInfo();
                nat.setName("display" + (uri.getPort() - 5900));
                nat.setPort(uri.getPort());
                assignPortMapping(nat);
                enablePortMapping(nat);
                return nat.getMappedPort();
            }
        }
        return uri.getPort();
    }
}
