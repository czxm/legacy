package com.intel.cedar.service.client.feature.model;

import com.intel.cedar.service.client.model.CedarBaseModel;

public class MachineFeature extends CedarBaseModel {
    /**
	 * 
	 */
    private static final long serialVersionUID = 4125635169606256797L;
    private String osName;
    private String arch;

    public MachineFeature() {

    }

    public MachineFeature(String osName, String arch) {
        this.setOsName(osName);
        this.setArch(arch);
    }

    public void refresh() {
        set("OSName", osName);
        set("Arch", arch);
    }

    public void setArch(String arch) {
        this.arch = arch;
    }

    public String getArch() {
        return arch;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getOsName() {
        return osName;
    }
}
