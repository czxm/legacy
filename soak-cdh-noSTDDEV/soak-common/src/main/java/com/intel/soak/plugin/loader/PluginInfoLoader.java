package com.intel.soak.plugin.loader;

import com.intel.soak.plugin.dto.PluginInfo;
import com.intel.soak.utils.JarFileResource;

/**
 * Read plugin metadata from plugin bundle.
 * <p>
 * The source of metadata includes jar MANIFEST and spring configuration file in each
 * plugin bundle. User should specify plugin level metadata in jar MANIFEST and plugin
 * component level metadata in spring configuration file. Please refer to the
 * <a href ="http://bigdata-wiki.ic.intel.com/wiki/Create_a_Soak_Plugin_Bundle">doc</a>
 * for more details on creating a plugin bundle.
 *
 * @author Joshua Yao (yi.a.yao@intel.com)
 * @since 12/19/13 3:38 AM
 */
public interface PluginInfoLoader {

    /**
     * Fetch metadata of plugin and plugin components.
     *
     * @param plugin    plugin jar resource
     * @return          plugin metadata
     */
    PluginInfo fetchPluginInfo(JarFileResource plugin);

}
