package com.intel.soak.plugin.mapreduce.driver;

import com.intel.soak.driver.GenericDriver;
import com.intel.soak.plugin.annotation.PLUGIN_TYPE;
import com.intel.soak.plugin.annotation.Plugin;
import com.intel.soak.plugin.mapreduce.transaction.JarMapReduceTransaction;
import com.intel.soak.plugin.mapreduce.utils.DynamicJavaLibPathLoader;
import com.intel.soak.transaction.Transaction;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: joshua
 * Date: 12/2/13
 * Time: 11:52 PM
 * To change this template use File | Settings | File Templates.
 */
@Plugin(desc="Map Reduce Driver", type = PLUGIN_TYPE.DRIVER)
public class MapReduceDriver extends GenericDriver {

    protected Configuration conf = new Configuration();

    @Override
    public boolean startup() {
        try {
            String javaLibPath = getParamValue("java.library.path");
            if (StringUtils.isNotEmpty(javaLibPath))
                DynamicJavaLibPathLoader.addLibPath(javaLibPath);

            String mrSite = getParamValue("mr-site");
            if (StringUtils.isNotEmpty(mrSite))
                conf.addResource(new File(mrSite).toURI().toURL());

            String coreSite = getParamValue("core-site");
            if (StringUtils.isNotEmpty(coreSite))
                conf.addResource(new File(coreSite).toURI().toURL());

            boolean localForbidden = Boolean.valueOf(getParamValue("local-forbidden"));
            if (localForbidden && "local".equals(conf.get("mapred.job.tracker", "local"))) {
                throw new RuntimeException(
                        "This must be run in only the distributed mode!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void prepareTransaction(Transaction transaction) {
        if (transaction instanceof JarMapReduceTransaction) {
            // Clone the baseline Hadoop configuration to avoid resource competition.
            ((JarMapReduceTransaction) transaction).getExecutor()
                    .setConfiguration(new Configuration(this.conf));
        }
    }

}
