package com.intel.soak.plugin;

import com.intel.soak.Bootable;
import com.intel.soak.plugin.dto.PluginInfo;
import org.springframework.core.io.Resource;

import java.util.Collection;

/**
 * The abstraction of plugin master which is used to validate and register plugins.
 * <p>
 * Note that plugin master never loads plugin jars into classpath of SOAK framework.
 * Also, not any classes in plugins will be loaded.
 *
 * @author Joshua Yao (yi.a.yao@intel.com)
 * @since 12/18/13
 */
public interface PluginMaster extends Bootable {

    /**
     * Validate plugins in dropins dir. All legal plugins will be moved to plugins
     * dir after validation.
     */
    void validateDropins();

    /**
     * Validate specified dropins. The dropins will be moved to dropins dir if the
     * dropins are legal.
     *
     * @param dropins   plugin resources
     */
    void validateDropins(final Resource... dropins);

    /**
     * Validate a specified dropins. The dropins will be moved to plugins dir if the
     * plugins are legal.
     *
     * @param dropins   dropins resource
     */
    void validateDropin(final Resource dropins);

    /**
     * Register legal plugins in plugins dir to {@link com.intel.soak.plugin.manager.SimplePluginManager}
     */
    void registerPlugins();

    /**
     * Register specific legal plugins to {@link com.intel.soak.plugin.manager.SimplePluginManager}
     *
     * @param plugins   plugin resources, plugin means legal drop-in
     */
    void registerPlugins(final Resource... plugins);

    /**
     * Register specific legal plugin to {@link com.intel.soak.plugin.manager.SimplePluginManager}
     *
     * @param plugin   plugin resource, plugin means legal drop-in
     */
    void registerPlugin(final Resource plugin);

    /**
     * List all the plugins registered in SOAK.
     *
     * @return  plugin list.
     */
    Collection<PluginInfo> listPluginsInfo();

}
