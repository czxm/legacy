package com.intel.ca360.loadmeter.transaction;

public class LdapConnPoolRequest {
	private String server;
	private int port;
	private String authnId;
	private String passwd;
	private LdapAuthnType type;
	public String getServer() {
		return server;
	}
	public void setServer(String server) {
		this.server = server;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getAuthnId() {
		return authnId;
	}
	public void setAuthnId(String authnId) {
		this.authnId = authnId;
	}
	public String getPasswd() {
		return passwd;
	}
	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}
	public LdapAuthnType getType() {
		return type;
	}
	public void setType(LdapAuthnType type) {
		this.type = type;
	}	
}
