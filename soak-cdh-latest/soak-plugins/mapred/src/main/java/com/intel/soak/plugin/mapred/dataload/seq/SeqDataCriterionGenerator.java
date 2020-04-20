package com.intel.soak.plugin.mapred.dataload.seq;

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
 * Time: 10:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class SeqDataCriterionGenerator  implements IDataGenerator {

    @Override
    public void generate(DataLoaderConfig config) {
        String type = config.getDataType();
        if (!"ALL".equalsIgnoreCase(type)
                && !"SEQ".equalsIgnoreCase(type)) {
            return;
        }
        System.out.println("[Test][GenCriterion] Generate test seq criterion...");
        String[] args = {
                config.getTarget() + File.separator + "data" + File.separator + "seq",
                config.getTarget() + File.separator + "criterion" + File.separator + "seq",
        };
        try {
            int res = ToolRunner.run(new JobConf(Bootstrap.configuration),
                    new SeqWordCountCriterionGen(), args); // TODO
            if (res != 0) {
                throw new RuntimeException("Generate data failed");
            }
        } catch (Exception e) {
            System.err.println("Generate seq criterion failed: " + e.toString());
            System.exit(1);
        }
        System.out.println("[Test][GenCriterion] Generate seq testing criterion successfully...");
    }

}
