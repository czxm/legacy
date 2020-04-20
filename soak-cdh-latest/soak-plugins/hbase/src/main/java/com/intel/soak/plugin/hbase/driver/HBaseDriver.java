package com.intel.soak.plugin.hbase.driver;

import com.intel.soak.driver.GenericDriver;
import com.intel.soak.plugin.annotation.PLUGIN_TYPE;
import com.intel.soak.plugin.annotation.Plugin;
import com.intel.soak.plugin.hbase.transaction.HBaseBaseTransaction;
import com.intel.soak.transaction.Transaction;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;

/**
 * Created with IntelliJ IDEA.
 * User: xhao1
 * Date: 11/22/13
 * Time: 1:20 PM
 * To change this template use File | Settings | File Templates.
 */
@Plugin(desc = "HBaseDriver", type = PLUGIN_TYPE.DRIVER)

public class HBaseDriver extends GenericDriver {
    protected Configuration conf;

    @Override
    public boolean startup() {
        logger.info("## CLASSPATH: " + getParamValue("CLASSPATH"));

        try {
            conf = HBaseConfiguration.create();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public void prepareTransaction(Transaction transaction) {
        if (transaction instanceof HBaseBaseTransaction) {
            // Clone the baseline  configuration to avoid resource competition.
            ((HBaseBaseTransaction) transaction).setConf(new Configuration(this.conf));
        }
    }

}
