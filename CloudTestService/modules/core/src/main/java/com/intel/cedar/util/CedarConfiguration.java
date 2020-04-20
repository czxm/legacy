package com.intel.cedar.util;

import java.io.FileInputStream;
import java.util.Properties;

public class CedarConfiguration {
    private static CedarConfiguration singleton;

    public static synchronized CedarConfiguration getInstance() {
        if (singleton == null)
            singleton = new CedarConfiguration();
        return singleton;
    }

    private Properties props = new Properties();

    private CedarConfiguration() {
        reload();
    }

    public synchronized void reload() {
        FileInputStream input = null;
        try {
            props.clear();
            input = new FileInputStream(SubDirectory.CONFIG.toString()
                    + "cedar.conf");
            props.load(input);
        } catch (Exception e) {
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Exception e) {
                }
            }
        }
    }

    public synchronized Properties getProperties() {
        return this.props;
    }

    public synchronized String getStorageRoot() {
        return props.getProperty("StorageRoot", null);
    }

    public synchronized int getHistoryExpire() {
        try {
            return Integer.parseInt(props.getProperty("HistoryExpireTime"));
        } catch (Exception e) {
            return 3600 * 24 * 60; // 2 month
        }
    }

    public synchronized int getAgentTimeout() {
        try {
            return Integer.parseInt(props.getProperty("AgentTimeout"));
        } catch (Exception e) {
            return 600;
        }
    }

    public synchronized int getVolumeExpire() {
        try {
            return Integer.parseInt(props.getProperty("VolumeExpireTime"));
        } catch (Exception e) {
            return 3600 * 48;
        }
    }

    public synchronized int getMinimumAvailable() {
        try {
            return Integer.parseInt(props.getProperty("MinAvailInstances"));
        } catch (Exception e) {
            return 1;
        }
    }

    public synchronized int getMaximumVolumePool() {
        try {
            return Integer.parseInt(props.getProperty("MaxVolumePool"));
        } catch (Exception e) {
            return 500;
        }
    }

    public synchronized int getAllowedClockSkew() {
        try {
            return Integer.parseInt(props
                    .getProperty("AllowedClockSkew", "180"));
        } catch (Exception e) {
            return 180;
        }
    }
    
    public synchronized int getMaximumStandbyInstances() {
        try {
            return Integer.parseInt(props
                    .getProperty("MaxStandyInstances", "0"));
        } catch (Exception e) {
            return 0;
        }
    }
    
    public synchronized boolean getEnableTaskService() {
        try {
            return Boolean.parseBoolean(props
                    .getProperty("EnableTaskService", "true"));
        } catch (Exception e) {
            return true;
        }
    }

    public synchronized boolean getEnableCloudService() {
        try {
            return Boolean.parseBoolean(props
                    .getProperty("EnableCloudService", "true"));
        } catch (Exception e) {
            return true;
        }
    }
    
    public synchronized boolean getRemoveUnknownInstances() {
        try {
            return Boolean.parseBoolean(props
                    .getProperty("RemoveUnknownInstances", "false"));
        } catch (Exception e) {
            return false;
        }
    }
    
    public synchronized String getInterface() {
        return props.getProperty("interface");
    }

    public synchronized String getForwader() {
        return props.getProperty("Forwarder", "N/A");
    }

    public synchronized String getSMTPServer() {
        return props.getProperty("SMTPServer", "mail.intel.com");
    }

    public static String getServiceURL() {
        return "http://" + NetUtil.getHostAddress(getInstance().getInterface())
                + "/";
    }

    public static String getStorageServiceURL() {
        return "http://" + NetUtil.getHostName(getInstance().getInterface())
                + "/cloudtestservice/storage";
    }
}
