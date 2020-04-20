package com.intel.soak.plugin.manager;

import com.intel.soak.plugin.PluginFSM;

/**
 * Abstract implementation of Plugin Manager which is used to provide fsm of plugins.
 *
 * @author Joshua Yao (yi.a.yao@intel.com)
 * @since 12/19/13 2:45 AM
 */
public abstract class AbstractPluginManager implements IPluginManager {

    /**
     * Plugin FSM
     */
    protected PluginFSM fsm = PluginFSM.INSTANCE;

}
