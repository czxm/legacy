package com.intel.soak.plugin.mapred.dataload.txt;

import com.intel.soak.plugin.mapred.Bootstrap;
import com.intel.soak.plugin.mapred.dataload.DataLoaderConfig;
import com.intel.soak.plugin.mapred.dataload.IDataGenerator;
import org.apache.hadoop.examples.terasort.TeraGen;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.util.ToolRunner;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: joshua
 * Date: 11/12/13
 * Time: 9:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class TxtDataGenerator implements IDataGenerator {

    @Override
    public void generate(DataLoaderConfig config) {
        String type = config.getDataType();
        if (!"ALL".equalsIgnoreCase(type)
                && !"TXT".equalsIgnoreCase(type)) {
            return;
        }

        System.out.println("[Test][GenData] Generate tera data for testing...");

        // Each tera record size is 100B
        String[] args = {String.valueOf(config.getRecords()),
                config.getTarget() + File.separator + "data" + File.separator + "txt"};
        try {
            int res = ToolRunner.run(new JobConf(Bootstrap.configuration), new TeraGen(), args); // TODO
        } catch (Exception e) {
            System.err.println("Generate data failed! " + e.toString());
        }
        System.out.println("[Test][GenData] Generate tera data for testing successfully...");
    }

}
