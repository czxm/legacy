package com.intel.soak.plugin.mapred;

import com.intel.soak.plugin.mapred.dataload.DataGenerator;
import com.intel.soak.plugin.mapred.dataload.DataLoaderConfig;
import org.apache.commons.cli.*;
import org.apache.hadoop.conf.Configuration;

/**
 * Created with IntelliJ IDEA.
 * User: joshua
 * Date: 11/12/13
 * Time: 8:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class Bootstrap {

    private static Options options = new Options();

    static {
        options.addOption("l", "load", false, "Prepare test data for MR soak test");
        options.addOption(OptionBuilder.withLongOpt("type")
                .withDescription("data type: [ALL|TXT|SEQ]")
                .hasArg()
                .withArgName("DATA_TYPE")
                .isRequired()
                .create()
        );
        options.addOption(OptionBuilder.withLongOpt("records")
                .withDescription("record count")
                .hasArg()
                .withArgName("RECORD_COUNT")
                .isRequired()
                .create()
        );
        options.addOption(OptionBuilder.withLongOpt("target")
                .withDescription("The data dir on HDFS")
                .hasArg()
                .withArgName("DIR")
                .isRequired()
                .create()
        );
        options.addOption(OptionBuilder.withLongOpt("hdfsconf")
                .withDescription("core-site.xml dir")
                .hasArg()
                .withArgName("HDFS_DIR")
                .isRequired()
                .create()
        );
        options.addOption(OptionBuilder.withLongOpt("mrconf")
                .withDescription("mapred-site.xml dir")
                .hasArg()
                .withArgName("MR_DIR")
                .isRequired()
                .create()
        );
    }

    private static void exception(String msg) {
        System.err.println("ERROR: " + msg);
        System.exit(1);
    }

    public static Configuration configuration = new Configuration();

    public static void main(String[] args) {
//        args = new String[]{
//                "-l",
//                "--type", "ALL",
//                "--records", "100",
//                "--target", "/tmp",
//                "--hdfsconf", "/usr/lib/hadoop/core-site.xml",
//                "--mrconf", "/usr/lib/hadoop/mapred-site.xml"
//        };
        CommandLineParser parser = new BasicParser();
        try {
            CommandLine cli = parser.parse(options, args);
            String type = cli.getOptionValue("type");
            if (!"All".equalsIgnoreCase(type)
                    && !"TXT".equalsIgnoreCase(type)
                    && !"SEQ".equalsIgnoreCase(type)) {
                exception("Data type should be ALL or TXT or SEQ");
            }
            long records = 0;
            try {
                records = Long.parseLong(cli.getOptionValue("records"));
            } catch (Exception e) {
                exception("records should be a long value.");
            }
            String dir = cli.getOptionValue("target");
            String coresite = cli.getOptionValue("hdfsconf");
            String mrsite = cli.getOptionValue("mrconf");

            DataLoaderConfig config = new DataLoaderConfig(type,
                    records, dir, coresite, mrsite);

            System.out.println(String.format("Generate %d records...", records));
            System.out.println("Data type: " + type);
            System.out.println("Data dir: " + dir);
            System.out.println("core-site.xml: " + coresite);
            System.out.println("mapred-site.xml: " + mrsite);

            configuration.addResource(mrsite);
            configuration.addResource(coresite);

            new DataGenerator().gen(config);
        } catch (ParseException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

}
