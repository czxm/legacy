package com.intel.soak.plugin.mapred.dataload.seq;

import com.intel.soak.plugin.mapred.Bootstrap;
import com.intel.soak.plugin.mapred.dataload.DataLoaderConfig;
import com.intel.soak.plugin.mapred.dataload.IDataGenerator;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;

import java.io.File;
import java.util.Random;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: joshua
 * Date: 11/12/13
 * Time: 10:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class SeqDataGenerator implements IDataGenerator {

    private static final int VALUE_LENGTH = 25;

    @Override
    public void generate(DataLoaderConfig config) {
        String type = config.getDataType();
        if (!"ALL".equalsIgnoreCase(type)
                && !"SEQ".equalsIgnoreCase(type)) {
            return;
        }
        System.out.println("[Test][GenData] Generate sequence data for testing...");
        try {
            genData(config);
        } catch (Exception e) {
            System.err.println("Generate data failed! " + e.toString());
        }
        System.out.println("[Test][GenData] Generate sequence data for testing successfully...");
    }

    private void genData(DataLoaderConfig config) throws Exception {
        SequenceFile.Writer writer = null;
        try {
            FileSystem fs = FileSystem.get(Bootstrap.configuration);
            Path path = new Path(config.getTarget() + File.separator + "data" + File.separator + "seq");
            if (fs.exists(path)) {
                fs.delete(path, true);
            }
            writer = SequenceFile.createWriter(fs,
                    Bootstrap.configuration, path, Text.class, Text.class);

            for (long i = 0; i < config.getRecords(); i++) {
                String key = UUID.randomUUID().toString();
                writer.append(new Text(key),
                        new Text(StringGenerator.genRandomString(VALUE_LENGTH)));
            }
            writer.syncFs();
        } finally {
            IOUtils.closeStream(writer);
        }

    }

    public static class StringGenerator {
        private static final StringBuilder sb = new StringBuilder(
                "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");

        private static Random RANDOM = new Random();

        public static String genRandomString(int length) {
            StringBuilder str = new StringBuilder();

            //TODO: Performance...
            for (int i = 0; i < length; i++) {
                str.append(sb.charAt(RANDOM.nextInt(sb.length())));
            }
            return str.toString();
        }
    }

}
