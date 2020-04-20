package com.intel.cedar.pool;

import java.util.Properties;

import com.intel.cedar.core.entities.MachineInfo;
import com.intel.cedar.engine.model.feature.Tasklet;

public class ResourceRequest {
    private String taskletId;
    private Long userId;
    private String featureId;
    private boolean reproducable;
    private Tasklet.Sharable sharable;

    private int cpu;
    private int mem;
    private int disk;
    private int count;
    private MachineInfo.OS os;
    private MachineInfo.ARCH arch;
    private String host;
    private Properties properties;
    private boolean recycle;
    private boolean visible;

    public ResourceRequest(String taskletId, Long userId, String featureId,
            boolean reproducable, Tasklet.Sharable sharable) {
        this.taskletId = taskletId;
        this.userId = userId;
        this.featureId = featureId;
        this.reproducable = reproducable;
        this.sharable = sharable;

        cpu = 1;
        mem = 1;
        disk = 1;
        count = 1;
        os = MachineInfo.OS.any;
        arch = MachineInfo.ARCH.any;
        properties = new Properties();
    }

    public void setMachineRequest(String host, MachineInfo.OS os,
            MachineInfo.ARCH arch, int cpu, int mem, int disk, int count,
            Properties properties,
            boolean recycle, boolean visible) {
        this.host = host;
        this.cpu = cpu;
        this.mem = mem;
        this.disk = disk;
        this.count = count;
        this.os = os;
        this.arch = arch;
        this.properties = properties;
        this.recycle = recycle;
        this.visible = visible;
    }

    public String getTaskletId() {
        return taskletId;
    }

    public void setTaskletId(String taskletId) {
        this.taskletId = taskletId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFeatureId() {
        return featureId;
    }

    public void setFeatureId(String featureId) {
        this.featureId = featureId;
    }

    public boolean isReproducable() {
        return reproducable;
    }

    public void setReproducable(boolean reproducable) {
        this.reproducable = reproducable;
    }

    public Tasklet.Sharable getSharable() {
        return sharable;
    }

    public int getCpu() {
        return cpu;
    }

    public void setCpu(int cpu) {
        this.cpu = cpu;
    }

    public int getMem() {
        return mem;
    }

    public void setMem(int mem) {
        this.mem = mem;
    }

    public int getDisk() {
        return disk;
    }

    public void setDisk(int disk) {
        this.disk = disk;
    }

    public MachineInfo.OS getOs() {
        return os;
    }

    public void setOs(MachineInfo.OS os) {
        this.os = os;
    }

    public MachineInfo.ARCH getArch() {
        return arch;
    }

    public void setArch(MachineInfo.ARCH arch) {
        this.arch = arch;
    }

    public String getHost() {
        return this.host;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public int getCount() {
        return count;
    }
    
    public boolean getRecycle(){
        return this.recycle;
    }
    
    public boolean getVisible(){
        return this.visible;
    }
}
