package com.intel.bigdata.common.protocol;

public class ServiceInstantStatus {

	private String node;
	
	private String status;

	public ServiceInstantStatus() {
		this(null, null);
	}
	
	public ServiceInstantStatus(String node, String status) {
		this.node = node;
		this.status = status;
	}
	
	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
}
