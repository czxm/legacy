package com.intel.soak.plugin.hdfs.stability.transaction;

import com.intel.soak.plugin.annotation.PLUGIN_TYPE;
import com.intel.soak.plugin.annotation.Plugin;
import com.intel.soak.transaction.AbstractTransaction;

/**
 * Created with IntelliJ IDEA.
 * User: jzhu61
 * Date: 11/19/13
 * Time: 5:47 PM
 * To change this template use File | Settings | File Templates.
 */

@Plugin(desc = "HDFSDecomTransaction", type = PLUGIN_TYPE.TRANSACTION)

public class HDFSDecomTransaction extends AbstractTransaction {
    @Override
    public boolean execute() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void kill() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
