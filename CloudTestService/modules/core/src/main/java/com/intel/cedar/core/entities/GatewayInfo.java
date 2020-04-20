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

import com.intel.cedar.util.EntityUtil;
import com.intel.cedar.util.EntityWrapper;

@Entity
@PersistenceContext(name = "cedar_general")
@Table(name = "gateways")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class GatewayInfo extends AbstractHostInfo implements
        Comparable<GatewayInfo> {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id = -1l;
    @Column(name = "nodeName")
    // the unique name for this physical node
    private String nodeName;
    // total mapped ports, this is cached from the gateway
    // there will be a daemon to refresh this value
    @Column(name = "mappedPorts")
    private Integer mappedPorts;
    @Column(name = "intf")
    private String intf;

    public static GatewayInfo load(Long id) {
        EntityWrapper<GatewayInfo> db = EntityUtil.getGatewayEntityWrapper();
        try {
            return db.load(GatewayInfo.class, id);
        } finally {
            db.rollback();
        }
    }

    public GatewayInfo() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getMappedPorts() {
        if (mappedPorts == null) {
            mappedPorts = 0;
        }
        return mappedPorts;
    }

    public void setMappedPorts(Integer mappedPorts) {
        this.mappedPorts = mappedPorts;
    }

    public int compareTo(GatewayInfo o) {
        return this.getId().compareTo(o.getId());
    }

    public String getIntf() {
        return intf;
    }

    public void setIntf(String intf) {
        this.intf = intf;
    }

    public boolean setAdminPasswd(String passwd) {
        throw new RuntimeException("Not supported for now");
    }

    public MachineInfo.OS getOs() {
        throw new RuntimeException("Not supported for now");
    }

    public MachineInfo.ARCH getArch() {
        throw new RuntimeException("Not supported for now");
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public List<String> getCapabilities() {
        throw new RuntimeException("Not supported for now");
    }

    public Properties getProperties() {
        throw new RuntimeException("Not supported for now");
    }

    @Override
    public boolean isValid() {
        return load(getId()) != null;
    }

    @Override
    public String getRootPath() {
        throw new RuntimeException("Not supported for now");
    }

    @Override
    public void assignPortMapping(NATInfo i) {
        throw new RuntimeException("Not supported for now");
    }

    @Override
    public List<NATInfo> getPortMappings() {
        throw new RuntimeException("Not supported for now");
    }
}
