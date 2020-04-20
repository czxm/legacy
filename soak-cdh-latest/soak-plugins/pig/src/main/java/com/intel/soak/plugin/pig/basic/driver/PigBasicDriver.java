package com.intel.soak.plugin.pig.basic.driver;

import com.intel.bigdata.common.util.Command;
import com.intel.soak.driver.GenericDriver;
import com.intel.soak.plugin.annotation.PLUGIN_TYPE;
import com.intel.soak.plugin.annotation.Plugin;

/**
 * Created with IntelliJ IDEA.
 * User: root
 * Date: 12/16/13
 * Time: 9:36 AM
 * To change this template use File | Settings | File Templates.
 */

@Plugin(desc = "PigBasicDriver", type = PLUGIN_TYPE.DRIVER)
public class PigBasicDriver extends GenericDriver {

    @Override
    public boolean startup(){
        return true;
    }
}
