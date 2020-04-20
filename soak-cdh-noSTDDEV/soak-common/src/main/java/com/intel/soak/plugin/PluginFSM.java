package com.intel.soak.plugin;

import com.intel.soak.plugin.dto.PluginInfo;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: joshua
 * Date: 10/29/13
 * Time: 4:04 AM
 * To change this template use File | Settings | File Templates.
 */
public enum PluginFSM {

    INSTANCE;

    public static enum PLUGIN_STATUS
            implements Serializable {
        NEW,
        INSTALL,
        ONLINE,
        OFFLINE,
        UNINSTALL,
        ERROR;
    }

    public static enum PLUGIN_OPTS
            implements Serializable {
        INSTALL,
        REGISTER,
        LOGOUT,
        UNINSTALL,
        ERROR;
    }

    public void stateChange(final PluginInfo plugin, PLUGIN_OPTS opt) {
        stateChange(plugin, opt, null);
    }

    public void stateChange(final PluginInfo plugin, PLUGIN_OPTS opt, Boolean result) {
        PLUGIN_STATUS currentStatus = plugin.getStatus();
        switch (currentStatus) {
            case NEW:
                if (opt == PLUGIN_OPTS.INSTALL && result != null && result)
                    plugin.setStatus(PLUGIN_STATUS.INSTALL);
                break;

            case INSTALL:
                if (opt == PLUGIN_OPTS.REGISTER && result != null && result)
                    plugin.setStatus(PLUGIN_STATUS.ONLINE);
                else
                    plugin.setStatus(PLUGIN_STATUS.OFFLINE);
                break;

            case ONLINE:
                if (opt == PLUGIN_OPTS.LOGOUT && result == null && result)
                    plugin.setStatus(PLUGIN_STATUS.OFFLINE);
                break;

            case OFFLINE:
                if (opt == PLUGIN_OPTS.UNINSTALL)
                    plugin.setStatus(PLUGIN_STATUS.UNINSTALL);
                break;

            default:
                if (opt == PLUGIN_OPTS.ERROR) {
                    plugin.setStatus(PLUGIN_STATUS.ERROR);
                } else {
                    throw new RuntimeException(String.format(
                            "No such state change for plugin [%s]. Current state: [%s], operation: [%s]",
                            plugin.getName(), plugin.getStatus().name(), opt.name()));
                }
        }
    }

}
