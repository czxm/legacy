package com.intel.soak.plugin.manager;

import com.intel.soak.plugin.dto.PluginInfo;

import java.util.Collection;

/**
 * Interface of plugin manager. All the plugin manager implementations must
 * implement the interface.
 * <p>
 * Plugin manager is used to control lifecycle of plugins and provide plugin
 * registration and search related functions.
 *
 * @author: Joshua Yao (yi.a.yao@intel.com)
 * @since: 12/19/13 2:33 AM
 */
public interface IPluginManager {

    /**
     * Register plugin metadata to plugin manager.
     *
     * @param pluginsInfo       plugins metadata
     */
    void register(final PluginInfo... pluginsInfo);

    /**
     * Search plugin metadata list
     *
     * @return  plugin metadata list
     */
    Collection<PluginInfo> listPlugins();

}
