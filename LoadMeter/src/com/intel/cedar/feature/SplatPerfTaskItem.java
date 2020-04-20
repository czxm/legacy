package com.intel.cedar.feature;

import com.intel.cedar.tasklet.SimpleTaskItem;


public class SplatPerfTaskItem extends SimpleTaskItem {
	private static final long serialVersionUID = 4022865146574946507L;
	private String scenario;
	private int users;
	private int userIndex;
	private int duration;
	private boolean monitorRemoteHosts;
	private String resultFile;
	private String server;
	private int clientCount;
	private boolean isBatch;
	private boolean isMultiTenance;
	private int tenances;
	private boolean isMultiIWA;
	private float negRate;
	private boolean updateCredOnly;
	private boolean doLogout;
	private int numSamlApps;
	private int numPostApps;
	private int pid;
	private String jMainClz;
	private int delay;
	
	public boolean isMultiIWA() {
		return isMultiIWA;
	}
	public void setMultiIWA(boolean isMultiIWA) {
		this.isMultiIWA = isMultiIWA;
	}
	public int getTenances() {
		return tenances;
	}
	public void setTenances(int tenances) {
		this.tenances = tenances;
	}
	public boolean isMultiTenance() {
		return isMultiTenance;
	}
	public void setMultiTenance(boolean isMultiTenance) {
		this.isMultiTenance = isMultiTenance;
	}
	public int getClientCount() {
		return clientCount;
	}
	public void setClientCount(int clientCount) {
		this.clientCount = clientCount;
	}
	public int getUsers() {
		return users;
	}
	public void setUsers(int users) {
		this.users = users;
	}
	public int getUserIndex() {
		return userIndex;
	}
	public void setUserIndex(int userIndex) {
		this.userIndex = userIndex;
	}
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
	public boolean isMonitorRemoteHosts() {
		return monitorRemoteHosts;
	}
	public void setMonitorRemoteHosts(boolean monitorRemoteHosts) {
		this.monitorRemoteHosts = monitorRemoteHosts;
	}
	public String getResultFile() {
		return resultFile;
	}
	public void setResultFile(String result) {
		this.resultFile = result;
	}
	public String getServer() {
		return server;
	}
	public void setServer(String server) {
		this.server = server;
	}
	public boolean isBatch() {
		return isBatch;
	}
	public void setBatch(boolean isBatch) {
		this.isBatch = isBatch;
	}
	public String getScenario() {
		return scenario;
	}
	public void setScenario(String scenario) {
		this.scenario = scenario;
	}
	public float getNegRate() {
		return negRate;
	}
	public void setNegRate(float negRate) {
		this.negRate = negRate;
	}
	public boolean isUpdateCredOnly() {
		return updateCredOnly;
	}
	public void setUpdateCredOnly(boolean updateCredOnly) {
		this.updateCredOnly = updateCredOnly;
	}	
	public boolean isDoLogout() {
		return doLogout;
	}
	public void setDoLogout(boolean doLogout) {
		this.doLogout = doLogout;
	}	
	public int getNumSamlApps() {
		return numSamlApps;
	}
	public void setNumSamlApps(int numSamlApps) {
		this.numSamlApps = numSamlApps;
	}
	public int getNumPostApps() {
		return numPostApps;
	}
	public void setNumPostApps(int numPostApps) {
		this.numPostApps = numPostApps;
	}
	public int getPid() {
		return pid;
	}
	public void setPid(int pid) {
		this.pid = pid;
	}	
	public int getDelay() {
		return delay;
	}
	public void setDelay(int delay) {
		this.delay = delay;
	}
	
	public String getJavaMainClass() {
		return jMainClz;
	}
	public void setJavaMainClass(String jMainClz) {
		this.jMainClz = jMainClz;
	}
	@Override
	public String getValue() {
		if(monitorRemoteHosts){
			return "MonitorItem";
		}
		else{
			return super.getValue();
		}
	}	
}
