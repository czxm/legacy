package com.intel.cedar.service.client.model;

import com.extjs.gxt.ui.client.data.BaseModel;

public class PhysicalNodeInfoBean extends BaseModel {

    /**
	 * 
	 */
    private static final long serialVersionUID = 7343242803829348790L;

    private Long id = -1L;
    private Long cloudId;
    private Long userId;
    private String host;
    private String osName;
    private String archName;
    private Integer cpu;
    private Integer mem;
    private Integer disk;
    private String rootPath;
    private Boolean managed;
    private Boolean shared;
    private Boolean pooled;
    private String cloudName;
    private String state;
    private String user;
    private String comment;

    public PhysicalNodeInfoBean() {

    }

    public PhysicalNodeInfoBean(String host, String osName, String archName,
            Integer cpu, Integer mem, Integer disk, String rootPath) {
        setHost(host);
        setOsName(osName);
        setArchName(archName);
        setCpu(cpu);
        setMem(mem);
        setDisk(disk);
        setRootPath(rootPath);
        setState("running");
    }

    public void refresh() {
        set("Id", id);
        set("Host", host);
        set("CloudName", cloudName);
        set("Os", osName);
        set("Arch", archName);
        set("Cpu", cpu);
        set("Mem", mem);
        set("RootPath", rootPath);
        set("Disk", disk);
        set("State", state);
        set("User", user);
        set("Comment", comment);
    }

    public void setId(Long id) {
        this.id = id;
        set("Id", id);
    }

    public Long getId() {
        return id;
    }

    public void setCloudId(Long cloudId) {
        this.cloudId = cloudId;
        set("CloudId", cloudId);
    }

    public Long getCloudId() {
        return cloudId;
    }

    public void setHost(String host) {
        this.host = host;
        set("Host", host);
    }

    public String getHost() {
        return host;
    }

    public void setCloudName(String cloudName) {
        this.cloudName = cloudName;
        set("CloudName", cloudName);
    }

    public String getCloudName() {
        return cloudName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
        set("OsName", osName);
    }

    public String getOsName() {
        return osName;
    }

    public void setArchName(String archName) {
        this.archName = archName;
        set("ArchName", archName);
    }

    public String getArchName() {
        return archName;
    }

    public void setCpu(Integer cpu) {
        this.cpu = cpu;
        set("Cpu", cpu);
    }

    public Integer getCpu() {
        return cpu;
    }

    public void setMem(Integer mem) {
        this.mem = mem;
        set("Mem", mem);
    }

    public Integer getMem() {
        return mem;
    }

    public void setDisk(Integer disk) {
        this.disk = disk;
        set("Disk", disk);
    }

    public Integer getDisk() {
        return disk;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
        set("RootPath", rootPath);
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
        set("UserId", userId);
    }

    public Long getUserId() {
        return userId;
    }

    public void setManaged(Boolean managed) {
        this.managed = managed;
        set("Managed", managed);
    }

    public Boolean getManaged() {
        return managed;
    }

    public Boolean getShared() {
        return shared;
    }

    public void setShared(Boolean shared) {
        this.shared = shared;
    }

    public Boolean getPooled() {
        return pooled;
    }

    public void setPooled(Boolean pooled) {
        this.pooled = pooled;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

}
