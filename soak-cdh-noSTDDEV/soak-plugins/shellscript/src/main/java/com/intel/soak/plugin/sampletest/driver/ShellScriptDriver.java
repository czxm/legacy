package com.intel.soak.plugin.sampletest.driver;

import com.intel.soak.driver.GenericDriver;
import com.intel.soak.model.ParamType;
import com.intel.soak.plugin.annotation.PLUGIN_TYPE;
import com.intel.soak.plugin.annotation.Plugin;

import java.util.List;

@Plugin(desc="shellscript Driver", type = PLUGIN_TYPE.DRIVER)
public class ShellScriptDriver extends GenericDriver {

	@Override
	public void setParams(List<ParamType> params) {
		try {
			Thread.sleep(100);
			System.out.println("Preparing for shell script!");
		} catch (InterruptedException e) {
		}
	}

}
