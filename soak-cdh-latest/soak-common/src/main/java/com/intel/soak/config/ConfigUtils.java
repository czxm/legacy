package com.intel.soak.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.intel.soak.utils.LoadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.soak.model.LoadConfig;
import com.intel.soak.model.MergeConfig;
import com.intel.soak.model.ParamType;
import com.intel.soak.model.TransactionType;

public class ConfigUtils {
    protected static Logger LOG = LoggerFactory.getLogger(ConfigUtils.class);
    
    public static List<Object> parseParams(String[] args) {
        if (args == null || args.length < 1) {
            LOG.error("Invalid arguments for LoadMeter!");
            System.exit(0);
        }

        List<Object> configs = new ArrayList<Object>();
        for (int i = 0; i < args.length; i++) {
            String configFile = args[i];
            if (!new File(configFile).canRead()) {
                LOG.error("Cannot read config: " + configFile);
                System.exit(0);
            }
            Object config = new ConfigReader<LoadConfig>().load(configFile,
                    LoadConfig.class);
            if (config == null) {
                config = new ConfigReader<MergeConfig>().load(configFile,
                        MergeConfig.class);
            }
            if (config != null) {
                configs.add(config);
            } else {
                LOG.error("Invalid config: " + configFile);
                System.exit(0);
            }
        }
        return configs;
    }
    
    public static <T> List<T> collectConfig(List<Object> configs, Class<T> clz) {
        List<T> list = new ArrayList<T>();
        for (Object obj : configs) {
            try {
                list.add(clz.cast(obj));
            } catch (Exception e) {
            }
        }
        return list;
    }
    
    public static String getVUserFeeder(LoadConfig loadConfig){
        String provider = loadConfig.getVirtualUserConfig().getProvider();
        if (provider == null || provider.length() == 0) {
           provider = SoakConfig.defaultVUserFeeder;
        }
        return provider;
    }
    
    public static String getDriverName(LoadConfig loadConfig){
        String driverName = loadConfig.getTaskConfig().getTaskDriver().getDriver();
        if (driverName == null || driverName.length() == 0) {
           driverName = SoakConfig.defaultDriver;
        }
        return driverName;
    }
    
    public static int getTaskDuration(LoadConfig loadConfig){
        int duration = 0;
        Integer durationInt = loadConfig.getTaskConfig().getDuration();
        if (durationInt != null) {
           duration = durationInt;
        }
        return duration;
    }
    
    public static List<ParamType> getDriverParams(LoadConfig loadConfig){
        return loadConfig.getTaskConfig().getTaskDriver().getParam();
    }
    
    public static String getDriverParam(LoadConfig loadConfig, String name){
        for(ParamType p : loadConfig.getTaskConfig().getTaskDriver().getParam()){
            if(p.getName().equals(name)){
                return p.getValue();
            }
        }
        return null;
    }
    
    public static List<TransactionType> getTransactions(LoadConfig loadConfig){
        return loadConfig.getTaskConfig().getTaskDriver().getTransaction();
    }

    public static List<MergeConfig> genImmediateMergeConfigs(List<LoadConfig> loadList) {
        List<MergeConfig> immediateMerge = new ArrayList<MergeConfig>();
        for (LoadConfig config : loadList) {
            MergeConfig c = LoadUtils.getMergeByLoad(config);
            if (c != null) {
                immediateMerge.add(c);
            }
        }
        return immediateMerge;
    }

}
