package com.intel.soak.plugin.dto;

import com.intel.soak.plugin.annotation.PLUGIN_TYPE;

import java.io.Serializable;

public class PluginComponent implements Serializable {

    private String id;
	private PLUGIN_TYPE type;
	private String name;
	private String desc;
    private PluginInfo plugin;

    public PluginComponent(String id, PluginInfo plugin) {
        this.id = id;
        this.plugin = plugin;
    }

	public PLUGIN_TYPE getType() {
		return type;
	}

	public void setType(PLUGIN_TYPE type) {
		this.type = type;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PluginInfo getPlugin() {
        return plugin;
    }

    public void setPlugin(PluginInfo plugin) {
        this.plugin = plugin;
    }
}
