package com.intel.soak.plugin.constants;

import com.intel.soak.config.SoakConfig;

/**
 * Created with IntelliJ IDEA.
 * User: joshua
 * Date: 10/29/13
 * Time: 4:44 AM
 * To change this template use File | Settings | File Templates.
 */
public interface PluginConstants {

    // plugin dirs
    String PLUGIN_DIR = SoakConfig.Dir.Plugins.toString();
    String DROP_INS_DIR = SoakConfig.Dir.Dropins.toString();
    String RUNTIME_PLUGIN_DIR = SoakConfig.Dir.RuntimePlugins.toString();
    String AGENT_RUNTIME_PLUGIN_DIR = SoakConfig.Dir.AgentRuntimePlugins.toString();

    // plugin manifest related
    String PLUGIN_NAME_KEY = "Plugin-Name";
    String PLUGIN_VERSION_KEY = "Plugin-Version";
    String PLUGIN_DESC_KEY = "Plugin-Desc";
    String PLUGIN_CONF_SUFFIX = "-plugin.xml";

    // plugin server related
    String PLUGIN_SHARE_PROTOCOL = "http";
    int PLUGIN_SHARE_PORT_DEFAULT = 9447;

}
