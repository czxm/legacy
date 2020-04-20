package com.intel.soak.driver;

import com.intel.soak.plugin.annotation.PLUGIN_TYPE;
import com.intel.soak.plugin.annotation.Plugin;
import com.intel.soak.util.CMClientFactory;

@Plugin(desc="faults Driver", type = PLUGIN_TYPE.DRIVER)
public class FaultsDriver extends GenericDriver {

    @Override
    public boolean startup(){
        CMClientFactory.INSTANCE.setupFromParams(this.params);
        return true;
    }
}
