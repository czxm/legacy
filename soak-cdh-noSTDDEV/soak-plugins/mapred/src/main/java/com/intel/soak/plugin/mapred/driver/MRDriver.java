package com.intel.soak.plugin.mapred.driver;

import com.intel.soak.driver.GenericDriver;
import com.intel.soak.plugin.annotation.PLUGIN_TYPE;
import com.intel.soak.plugin.annotation.Plugin;
import com.intel.soak.plugin.mapred.encryption.DynamicJavaLibPathLoader;
import com.intel.soak.plugin.mapred.transaction.MRTransaction;
import com.intel.soak.transaction.Transaction;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: joshua
 * Date: 11/19/13
 * Time: 1:03 AM
 * To change this template use File | Settings | File Templates.
 */
@Plugin(desc="MR Driver", type = PLUGIN_TYPE.DRIVER)
public class MRDriver extends GenericDriver {

    protected Configuration conf;

    @Override
    public boolean startup() {
        try {
            conf = new Configuration();

            String javaLibPath = getParamValue("java.library.path");
            if (StringUtils.isNotEmpty(javaLibPath))
                DynamicJavaLibPathLoader.addLibPath(javaLibPath);

            String mrSite = getParamValue("mr-site");
            if (StringUtils.isNotEmpty(mrSite))
                conf.addResource(new File(mrSite).toURI().toURL());

            String coreSite = getParamValue("core-site");
            if (StringUtils.isNotEmpty(coreSite))
                conf.addResource(new File(coreSite).toURI().toURL());

            String yarnSite = getParamValue("yarn-site");
            if (StringUtils.isNotEmpty(yarnSite))
                conf.addResource(new File(yarnSite).toURI().toURL());
            else{
                if ("local".equals(conf.get("mapred.job.tracker", "local"))) {
                    throw new RuntimeException(
                            "This must be run in only the distributed mode!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void prepareTransaction(Transaction transaction) {
        if (transaction instanceof MRTransaction) {
            // Clone the baseline Hadoop configuration to avoid resource competition.
            ((MRTransaction) transaction).setConf(new Configuration(this.conf));
        }
    }

}
