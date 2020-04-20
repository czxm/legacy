package com.intel.soak.plugin.annotation;

import java.io.Serializable;

public enum PLUGIN_TYPE implements Serializable {
	
	UNKNOWN,
	DRIVER,
	TRANSACTION,
	FEEDER;
}
