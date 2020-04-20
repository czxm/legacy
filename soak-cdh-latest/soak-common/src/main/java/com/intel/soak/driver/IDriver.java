package com.intel.soak.driver;

import com.intel.soak.logger.DriverLogger;
import com.intel.soak.plugin.Pluggable;
import com.intel.soak.transaction.Transaction;

public interface IDriver extends Pluggable {
	
	void prepareTransaction(Transaction transaction);
	
	boolean startup();

	void shutdown();
	
    void setLogger(DriverLogger logger);
}
