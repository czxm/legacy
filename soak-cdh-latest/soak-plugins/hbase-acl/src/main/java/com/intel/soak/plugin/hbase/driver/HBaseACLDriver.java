package com.intel.soak.plugin.hbase.driver;

import com.intel.soak.driver.GenericDriver;
import com.intel.soak.plugin.annotation.PLUGIN_TYPE;
import com.intel.soak.plugin.annotation.Plugin;
import com.intel.soak.transaction.Transaction;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;

/**
 * Created with IntelliJ IDEA.
 * User: root
 * Date: 7/30/14
 * Time: 9:14 AM
 * To change this template use File | Settings | File Templates.
 */


@Plugin(desc = "HBaseACLDriver", type = PLUGIN_TYPE.DRIVER)
public class HBaseACLDriver extends GenericDriver {

    @Override
    public boolean startup(){
        return true;
    }
}
