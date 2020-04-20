package com.intel.soak.plugin.dto;

import com.intel.soak.plugin.PluginFSM;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PluginInfo implements Serializable {

	private String name;
	private String version;
	private String desc;
    private String path;
    private URL remotePath;
    private PluginFSM.PLUGIN_STATUS status = PluginFSM.PLUGIN_STATUS.NEW;

    private final Map<String, PluginComponent> components = new HashMap<String, PluginComponent>();

	public static class Builder {
		private final String name;
        private final String path;
        private URL remotePath;
		private String version = "Unknown";
		private String desc = "No Description.";

		public Builder(String name, String path, URL remotePath) {
			this.name = name;
            this.path = path;
            this.remotePath = remotePath;
		}

		public Builder version(String version) {
			if (StringUtils.isNotBlank(version))
				this.version = version;
			return this;
		}

		public Builder desc(String desc) {
			if (StringUtils.isNotBlank(desc))
				this.desc = desc;
			return this;
		}

		public PluginInfo build() {
			return new PluginInfo(this);
		}

	}

    public PluginInfo() {
        super();
    }

	public PluginInfo(Builder builder) {
		this.name = builder.name;
        this.path = builder.path;
        this.remotePath = builder.remotePath;
		this.version = builder.version;
		this.desc = builder.desc;
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	public String getDesc() {
		return desc;
	}

    public String getPath() {
        return path;
    }

    public PluginFSM.PLUGIN_STATUS getStatus() {
        return status;
    }

    public void setStatus(PluginFSM.PLUGIN_STATUS status) {
        this.status = status;
    }

    public void addComponent(final PluginComponent component) {
        this.components.put(component.getId(), component);   //TODO
    }

    public void setComponents(final Collection<PluginComponent> components) {
        this.components.clear();
        for (PluginComponent comp : components) {
            this.components.put(comp.getId(), comp);
        }
    }

    public Map<String, PluginComponent> getComponents() {
        return components;
    }

    public Collection<PluginComponent> listComponents() {
        return components.values();
    }

    public boolean exists(final String componentId) {
        return components.get(componentId) != null;
    }

    public PluginComponent getComponent(final String componentId) {
        return components.get(componentId);
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public URL getRemotePath() {
        return remotePath;
    }

    public void setRemotePath(URL remotePath) {
        this.remotePath = remotePath;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PluginInfo))
            return false;
        PluginInfo info = (PluginInfo) obj;
        return this.name.equals(info.getName());// && this.version.equals(info.getVersion());
    }

    public String toString() {
		return String.format("%-20s(%s)		%-10s       %s\n", this.name, this.version, this.status.name(), this.desc);
	}

}
