package com.intel.cedar.service.client.model;

import com.extjs.gxt.ui.client.data.BaseModel;

public class MachineTypeInfoBean extends BaseModel {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long cloudId;
    private String cloudName;
    private String type;
    private Integer cpu; // number of cpu
    private Integer memory; // GB
    private Integer disk; // GB
    private Integer secondDisk; // GB
    private String verbose;
    private Integer free;
    private Integer max;

    public MachineTypeInfoBean() {

    }

    public void refresh() {
        set("Id", id);
        set("CloudName", cloudName);
        set("Type", type);
        set("Cpu", cpu);
        set("Memory", memory);
        set("Disk", disk);
        set("SecondDisk", secondDisk);
        set("Free", free);
        set("Max", max);
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setCloudName(String cloudName) {
        this.cloudName = cloudName;
    }

    public String getCloudName() {
        return cloudName;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setCpu(Integer cpu) {
        this.cpu = cpu;
    }

    public Integer getCpu() {
        return cpu;
    }

    public void setMemory(Integer memory) {
        this.memory = memory;
    }

    public Integer getMemory() {
        return memory;
    }

    public void setDisk(Integer disk) {
        this.disk = disk;
    }

    public Integer getDisk() {
        return disk;
    }

    public void setSecondDisk(Integer disk) {
        this.secondDisk = disk;
    }

    public Integer getSecondDisk() {
        return secondDisk;
    }

    public void setFree(Integer free) {
        this.free = free;
    }

    public Integer getFree() {
        return free;
    }

    public void setCloudId(Long cloudId) {
        this.cloudId = cloudId;
    }

    public Long getCloudId() {
        return cloudId;
    }

    public void set(String verbose) {
        this.verbose = verbose;
    }

    public String getVerbose() {
        verbose = "CPU:" + cpu + " Mem:" + memory + "MB Disk:" + disk + "GB"
                + (secondDisk > 0 ? " SecondDisk:" + secondDisk + "GB" : "")
                + (free >= 0 ? ", " + free + " Available" : "");
        return verbose;
    }

    public Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

}
