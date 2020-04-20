package com.intel.soak.transaction;

import com.intel.soak.logger.TransactionLogger;
import com.intel.soak.plugin.Pluggable;
import com.intel.soak.vuser.VUserData;

public interface Transaction extends Pluggable {
	
	void setUserData(VUserData user);
	
	void setLogger(TransactionLogger logger);

	boolean startup();

	boolean beforeExecute();

	boolean execute();
	
	void kill();

	boolean afterExecute();

	void shutdown();
}
