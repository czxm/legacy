package com.intel.soak.plugin.manager;

import com.intel.soak.plugin.PluginFSM;
import com.intel.soak.plugin.dto.PluginInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple plugin manager which uses a {@link HashMap} to contain the plugins metadata.
 * Please specify 'singleton' scope to configure the bean in Spring.
 * <p/>
 * Plugin classes are not loaded or initialized. Only register plugin metadata.
 *
 * @author Joshua Yao (yi.a.yao@intel.com)
 * @since 12/18/13 10:20 PM
 */
public class SimplePluginManager extends AbstractPluginManager {

    protected Log logger = LogFactory.getLog(this.getClass());

    // key is plugin name
    protected final Map<String, PluginInfo> pluginList = new HashMap<String, PluginInfo>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void register(final PluginInfo... pluginsInfo) {
        for (PluginInfo pluginInfo : pluginsInfo) {
            register(pluginInfo);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<PluginInfo> listPlugins() {
        synchronized (pluginList) {
            return pluginList.values();
        }
    }

    private void registerRollback(final PluginInfo pluginInfo) {
        synchronized (pluginList) {
            pluginList.remove(pluginInfo.getName());
            fsm.stateChange(pluginInfo, PluginFSM.PLUGIN_OPTS.ERROR);
        }
    }

    private void register(final PluginInfo pluginInfo) {
        synchronized (pluginList) {
            if (pluginList.get(pluginInfo.getName()) != null) {
                logger.warn(String.format(
                        "Register an existing plugin: %s. Override.",
                        pluginInfo.getName()));
            }
            try {
                pluginList.put(pluginInfo.getName(), pluginInfo);
                fsm.stateChange(pluginInfo, PluginFSM.PLUGIN_OPTS.INSTALL, true);

                logger.info(String.format("Register plugin [%s] in Plugin Manager.",
                        pluginInfo.getName()));
            } catch (Throwable e) {
                logger.error(String.format(
                        "Failed to register plugin [%s] in Plugin Manager.",
                        pluginInfo.getName()));
                registerRollback(pluginInfo);
            }
        }
    }

}
