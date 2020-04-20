package com.intel.soak.plugin.mapred.dataload.txt;

import com.intel.soak.plugin.mapred.Bootstrap;
import com.intel.soak.plugin.mapred.dataload.DataLoaderConfig;
import com.intel.soak.plugin.mapred.dataload.IDataGenerator;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.util.ToolRunner;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: joshua
 * Date: 11/12/13
 * Time: 10:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class TxtDataCriterionGenerator implements IDataGenerator {

    @Override
    public void generate(DataLoaderConfig config) {
        String type = config.getDataType();
        if (!"ALL".equalsIgnoreCase(type)
                && !"TXT".equalsIgnoreCase(type)) {
            return;
        }

        System.out.println("[Test][GenCriterion] Generate test criterion...");
        String[] args = {
                config.getTarget() + File.separator + "data" + File.separator + "txt",
                config.getTarget() + File.separator + "criterion" + File.separator + "txt",
        };
        try {
            int res = ToolRunner.run(new JobConf(Bootstrap.configuration),
                    new WordCountCriterionGen(), args); // TODO
            if (res != 0) {
                throw new RuntimeException("Generate data failed");
            }
        } catch (Exception e) {
            System.err.println("Generate criteria failed: " + e.toString());
            System.exit(1);
        }
        System.out.println("[Test][GenCriterion] Generate testing criterion for testing successfully...");
    }

}
