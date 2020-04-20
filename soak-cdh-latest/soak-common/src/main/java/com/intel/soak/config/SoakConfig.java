package com.intel.soak.config;

import java.io.File;
import java.util.Properties;

public class SoakConfig {

    public static enum ConfigKey{
        GangliaHosts,
        SoakMaster,
        SoakSlave,
        GaugeMaster,
        GaugeSlave,
        GaugeStorage,
        GangliaAgent,
    }

    public static enum BaseDir{
        HOME(System.getProperty("soak.home") == null ? "" : System.getProperty("soak.home"));

        private String path;

        BaseDir(String path){
            this.path = path;
        }
        public String toString(){
            return this.path;
        }
        public File toFile(){
            return new File(this.path);
        }
    }

    public static enum Dir{
        Conf("conf"),
        Plugins("plugins"),        
        Dropins("dropins"),
        RuntimePlugins("runtime-plugins"),
        RuntimeResources("runtime-resources"),
        AgentRuntimePlugins("agent-runtime-plugins"),
        AgentRuntimeResources("agent-runtime-resources");

        private String path;

        Dir(String name){
            this.path = BaseDir.HOME.toString() + File.separator + name;
        }
        public String toString(){
            return this.path;
        }
        public File toFile(){
            return new File(this.path);
        }
    }

    public static final String CONFIG = "soakConfig";
    public static final String defaultDriver = "defaultDriver";
    public static final String defaultVUserFeeder = "defaultVUserFeeder";
    public static final String Container = "soakContainer";
    public static String TxLogger;
    public static String DrvLogger;
    
    private boolean localMode;
    private Properties config = new Properties();
    
    public void setConfigSource(String source) throws Exception {
        config.load(SoakConfig.class.getClassLoader().getResourceAsStream(source));
    }
    
    public void setLocalMode(boolean mode){
        this.localMode = mode;
        TxLogger = this.localMode ? "localTxLogger" : "agentTxLogger";
        DrvLogger = this.localMode ? "localDrvLogger" : "agentDrvLogger";
    }
    
    public boolean isLocalMode(){
        return localMode;
    }
    
    public String getConfig(String key){
        return config.getProperty(key);
    }
    
    public String getConfig(ConfigKey key){
        return config.getProperty(key.name());
    }
}
