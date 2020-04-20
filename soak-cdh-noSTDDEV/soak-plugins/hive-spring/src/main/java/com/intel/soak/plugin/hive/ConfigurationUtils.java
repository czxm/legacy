package com.intel.soak.plugin.hive;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.util.GenericOptionsParser;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

public enum ConfigurationUtils {

    INSTANCE;

    public static void addLibs(Configuration configuration, Resource... libs) {
        addResource(configuration, libs, "-libjars");
    }

    public static void addFiles(Configuration configuration, Resource... files) {
        addResource(configuration, files, "-files");
    }

    public static void addArchives(Configuration configuration, Resource... archives) {
        addResource(configuration, archives, "-archives");
    }

    private static void addResource(Configuration cfg, Resource[] args, String name) {
        if (null == cfg)
            throw new RuntimeException("a non-null configuration is required");

        List<String> list = new ArrayList<String>();

        try {
            if (args != null) {
                int count = args.length;
                list.add(name);

                StringBuilder sb = new StringBuilder();
                for (Resource res : args) {
                    if (res == null) continue;
                    sb.append(res.getURI().toString());
                    if (--count > 0) {
                        sb.append(",");
                    }
                }
                list.add(sb.toString());
            }

            new GenericOptionsParser(cfg, list.toArray(new String[list.size()]));
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static void addProperties(Configuration configuration, Properties properties) {
        if (null == configuration)
            throw new RuntimeException("A non-null configuration is required");
        if (properties != null) {
            Enumeration<?> props = properties.propertyNames();
            while (props.hasMoreElements()) {
                String key = props.nextElement().toString();
                configuration.set(key, properties.getProperty(key));
            }
        }
    }

    public static Configuration createFrom(Configuration original, Properties properties) {
        Configuration cfg = null;
        if (original != null) {
            cfg = (original instanceof JobConf ? new JobConf(original) : new Configuration(original));
        } else {
            cfg = new JobConf();
        }
        addProperties(cfg, properties);
        return cfg;
    }

}
