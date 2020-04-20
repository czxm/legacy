/**
 * 
 */
package com.intel.cedar.core.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.intel.cedar.core.entities.MachineInfo.ARCH;
import com.intel.cedar.core.entities.MachineInfo.OS;

@Entity
@PersistenceContext(name = "cedar_general")
@Table(name = "mapping")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class MachineMappingInfo {
    @Id
    @Column(name = "pattern")
    private String pattern;
    @Column(name = "os")
    private OS os;
    @Column(name = "arch")
    private ARCH arch;

    /**
	 * 
	 */
    public MachineMappingInfo() {
    }

    public MachineMappingInfo(String pattern, OS os, ARCH arch) {
        this.pattern = pattern;
        this.os = os;
        this.arch = arch;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
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
}
