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
import javax.persistence.PersistenceContext;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.common.collect.Lists;
import com.intel.cedar.util.EntityUtil;
import com.intel.cedar.util.EntityWrapper;
import com.intel.cedar.util.protocal.ModelStream;

@Entity
@PersistenceContext(name = "cedar_general")
@Table(name = "machines")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class MachineInfo implements Comparable<MachineInfo> {
    public static OS anyOS = OS.any;
    public static OS unknownOS = OS.unknown;
    public static ARCH anyArch = ARCH.any;
    public static ARCH unknownArch = ARCH.unknown;

    public enum OS {
        unknown, any, as5, sles10, ubuntu, openSolaris, win2k3, winxp, win2k8, win7, centos, as6, sles11, unix;
        public String getOSName() {
            if (this.name().equals("unix")) {
                return "Generic Linux/Unix";
            } else if (this.name().equals("as5")) {
                return "RedHat Advanced Server 5";
            } else if (this.name().equals("as6")) {
                return "RedHat Advanced Server 6";
            } else if (this.name().equals("centos")) {
                return "CentOS";
            } else if (this.name().equals("sles10")) {
                return "SuSE Linux Enterprise Server 10";
            } else if (this.name().equals("sles11")) {
                return "SuSE Linux Enterprise Server 11";
            } else if (this.name().equals("ubuntu")) {
                return "Ubuntu Linux";
            } else if (this.name().equals("openSolaris")) {
                return "Oracle OpenSolaris";
            } else if (this.name().equals("win2k3")) {
                return "Microsoft Windows Server 2003";
            } else if (this.name().equals("winxp")) {
                return "Microsoft Windows XP";
            } else if (this.name().equals("win2k8")) {
                return "Microsoft Windows Server 2008";
            } else if (this.name().equals("win7")) {
                return "Microsoft Windows 7";
            } else if (this.name().equals("any"))
                return "any OS";
            else
                return "unknown OS";
        }

        public boolean isWindows() {
            if (this.name().equals("win2k3") || this.name().equals("winxp")
                    || this.name().equals("win2k8")
                    || this.name().equals("win7"))
                return true;
            else
                return false;
        }

        public boolean isAnyOS() {
            return this.equals(any);
        }

        public static OS fromString(String v) {
            try {
                if (v == null || v.equals(""))
                    return OS.any;
                return OS.valueOf(v);
            } catch (Exception e) {
                if (v.equals("windows"))
                    return win2k3;
                else if (v.equals("linux"))
                    return as5;
                for (OS os : OS.values()) {
                    if (os.getOSName().toLowerCase().contains(v.toLowerCase()))
                        return os;
                }
            }
            throw new RuntimeException("Unsupported Operating System");
        }
    }

    public enum ARCH {
        unknown, any, x86, x86_64;
        public boolean isAnyArch() {
            return this.equals(any);
        }

        public static ARCH fromString(String v) {
            try {
                if (v == null || v.equals(""))
                    return ARCH.any;
                return ARCH.valueOf(v);
            } catch (Exception e) {
                if (v.contains("86"))
                    return x86;
                else if (v.equalsIgnoreCase("amd64"))
                    return x86_64;
                else if (v.equalsIgnoreCase("intel64"))
                    return x86_64;
                else if (v.equalsIgnoreCase("em64t"))
                    return x86_64;
            }
            throw new RuntimeException("Unsupported Operating System");
        }
    }

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id = -1l;
    @Column(name = "imageId")
    private String imageId;
    @Column(name = "cloudId")
    private Long cloudId; // valid only if it's a cloud managed machine
    @Column(name = "imageName")
    // cloud specific image name
    private String imageName;
    @Column(name = "os")
    private OS os;
    @Column(name = "arch")
    private ARCH arch;
    @Column(name = "caps", length = 4096)
    private String caps;
    @Column(name = "props", length = 4096)
    private String props;
    @Column(name = "managed")
    private Boolean managed;
    @Column(name = "enabled")
    private Boolean enabled;
    @Column(name = "comment")
    private String comment;

    public static MachineInfo load(Long id) {
        EntityWrapper<MachineInfo> db = EntityUtil.getMachineEntityWrapper();
        try {
            return db.load(MachineInfo.class, id);
        } finally {
            db.rollback();
        }
    }

    public MachineInfo() {
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

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
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

    public CloudInfo getCloudInfo() {
        for (CloudInfo cloud : EntityUtil.listClouds()) {
            if (cloud.getId().equals(cloudId))
                return cloud;
        }
        return null;
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

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public Boolean getManaged() {
        if (managed == null)
            return false;
        return managed;
    }

    public void setManaged(Boolean managed) {
        this.managed = managed;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int compareTo(MachineInfo o) {
        return this.getImageId().compareTo(o.getImageId());
    }
}
