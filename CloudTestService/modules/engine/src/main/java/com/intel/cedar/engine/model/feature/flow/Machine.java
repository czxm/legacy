package com.intel.cedar.engine.model.feature.flow;

import java.util.Properties;

import com.intel.cedar.engine.model.DataModel;
import com.intel.cedar.engine.model.IDataModelDocument;
import com.intel.cedar.engine.xml.StandardNames;

public class Machine extends DataModel {
    private Properties properties = new Properties();
    private MachineParameter machineCounts;
    private MachineParameter cpuNum;
    private MachineParameter memory;
    private MachineParameter disk;
    private String OS;
    private String ARCH;
    private String host;
    private boolean recycle;
    private boolean visible;

    public Machine(IDataModelDocument document) {
        super(document);
    }

    public Properties getProperties() {
        return this.properties;
    }

    public void addProperty(String name, String value) {
        properties.setProperty(name, value);
    }

    public void removeProperty(String name) {
        properties.remove(name);
    }

    public void setCount(MachineParameter mp) {
        this.machineCounts = mp;
    }

    public void setCount(int min, int max) {
        MachineParameter mp = new MachineParameter(StandardNames.CEDAR_COUNT,
                min, max);
        setCount(mp);
    }

    public MachineParameter getCount() {
        return this.machineCounts;
    }

    public void setCPU(MachineParameter mp) {
        this.cpuNum = mp;
    }

    public void setCPU(int min, int max) {
        MachineParameter mp = new MachineParameter(StandardNames.CEDAR_CPU,
                min, max);
        setCPU(mp);
    }

    public MachineParameter getCPU() {
        return this.cpuNum;
    }

    public void setMemory(MachineParameter mp) {
        this.memory = mp;
    }

    public void setMemory(int min, int max) {
        MachineParameter mp = new MachineParameter(StandardNames.CEDAR_MEM,
                min, max);
        setMemory(mp);
    }

    public MachineParameter getMemory() {
        return this.memory;
    }

    public void setDisk(MachineParameter mp) {
        this.disk = mp;
    }

    public void setDisk(int min, int max) {
        MachineParameter mp = new MachineParameter(StandardNames.CEDAR_DISK,
                min, max);
        setDisk(mp);
    }

    public MachineParameter getDisk() {
        return this.disk;
    }

    public String getOS() {
        return this.OS;

    }

    public void setOS(String os) {
        this.OS = os;
    }

    public String getARCH() {
        return this.ARCH;
    }

    public void setARCH(String arch) {
        this.ARCH = arch;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
    
    public boolean getRecycle(){
        return this.recycle;
    }
    
    public void setRecycle(boolean r){
        this.recycle = r;
    }
    
    public boolean getVisible(){
        return this.visible;
    }
    
    public void setVisible(boolean r){
        this.visible = r;
    }
}
