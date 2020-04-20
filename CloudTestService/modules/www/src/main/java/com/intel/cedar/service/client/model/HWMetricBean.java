package com.intel.cedar.service.client.model;

import java.io.Serializable;

import com.extjs.gxt.ui.client.data.BaseModelData;

public class HWMetricBean extends BaseModelData implements Serializable {
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    private String _cpu;
    private String _memory;
    private String _harddisk;

    public HWMetricBean(String cpu, String memory, String harddisk) {
        _cpu = cpu;
        set("Cpu", _cpu);
        _memory = memory;
        set("Memory", _memory);
        _harddisk = harddisk;
        set("Harddisk", _harddisk);
    }

    public String getCpu() {
        return _cpu;
    }

    public String getMemory() {
        return _memory;
    }

    public String getHarddisk() {
        return _harddisk;
    }

    public String toString() {
        return "HWMetricBean[cpu=" + _cpu + ", memory=" + _memory
                + ", harddisk=" + _harddisk + "]";
    }
}
