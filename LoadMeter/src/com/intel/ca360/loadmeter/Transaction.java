package com.intel.ca360.loadmeter;

public interface Transaction {
	public void setup(AuthzData authz);
	public boolean startup();
	public boolean beforeExecute();
	public boolean execute(boolean negative);
	public boolean afterExecute();
	public void shutdown();
}
