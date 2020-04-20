package com.intel.ca360.loadmeter;

public class AuthzData {
	private String userName;
	private String password;
	
	public AuthzData(String u, String p){
		this.userName = u;
		this.password = p;
	}
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String[] getPasswords(){
		return this.password.split(" ");
	}
}
