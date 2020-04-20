package com.intel.cedar.agent;

import java.util.Properties;
import java.io.FileInputStream;

import com.intel.cedar.util.SubDirectory;

public class AgentConfiguration {
    private static AgentConfiguration singleton;

    public static synchronized AgentConfiguration getInstance() {
        if (singleton == null)
            singleton = new AgentConfiguration();
        return singleton;
    }

    private Properties props = new Properties();

    private AgentConfiguration() {
        FileInputStream input = null;
        try {
            props.clear();
            input = new FileInputStream(SubDirectory.CONFIG.toString()
                    + "agent.conf");
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

    public Properties getProperties() {
        return this.props;
    }

    public String getCharacterEncoding() {
        return props.getProperty("encoding", "GB2312");
    }

    public Boolean getEnableRServer() {
        return Boolean
                .parseBoolean(props.getProperty("enableRServer", "false"));
    }

    public String getInterface() {
        return props.getProperty("interface");
    }
}
