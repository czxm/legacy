package com.intel.bigdata.common.protocol;

public class ServiceConfig {

	private String node;
	
	private String config;

	public ServiceConfig() {
		this(null, null);
	}
	
	public ServiceConfig(String node, String config) {
		this.node = node;
		this.config = config;
	}
	
	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
	}

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}
	
}
