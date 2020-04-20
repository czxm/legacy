package com.intel.soak.plugin.pig.basic.transaction;

import com.intel.bigdata.common.util.Command;
import com.intel.soak.plugin.annotation.PLUGIN_TYPE;
import com.intel.soak.plugin.annotation.Plugin;
import com.intel.soak.transaction.AbstractTransaction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: root
 * Date: 12/16/13
 * Time: 9:36 AM
 * To change this template use File | Settings | File Templates.
 */
@Plugin(desc = "PigBasicTransaction", type = PLUGIN_TYPE.TRANSACTION)
public class PigBasicTransaction extends AbstractTransaction {
    @Override
    public boolean execute() {
        try {
            boolean success = true;
            List<String> result = new ArrayList<String>();
            int ret = Command.executeWithOutput(result, 0,  "sh",getParamValue("testPigScriptEntry"),getUserData().getUsername(), getParamValue("pigPluginHome"), getParamValue("rootHomeOnHDFS"));
            for(String r : result){
                logger.info(r);
            }
            if(ret > 0){
                success = false;
            }
            return success;
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return false;
    }

    @Override
    public void kill() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
