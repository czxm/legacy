package com.intel.soak.plugin;

import com.intel.soak.plugin.WebContainerBootstrap;

/**
 * Plugin Manager for distributed mode.
 *
 * @author: Joshua Yao (yi.a.yao@intel.com)
 * @since: 12/21/13 1:56 AM
 */
public class ClusterPluginMaster extends AbstractPluginMaster {

    /** Bootstrap of a jetty server which is used to share plugins in cluster.
     */
    protected WebContainerBootstrap pool = new WebContainerBootstrap();

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() {
        super.start();
        pool.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        super.stop();
        pool.stop();
    }

}
