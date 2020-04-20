package com.intel.cedar.features.splitpoint.sanity;

public interface Transaction {
	public void setup();
	public boolean startup();
	public void beforeExecute();
	public boolean execute(boolean negative);
	public void afterExecute();
	public void shutdown();
}
