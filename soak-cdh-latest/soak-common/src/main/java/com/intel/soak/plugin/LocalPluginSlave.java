package com.intel.soak.plugin;

import com.intel.soak.config.SoakConfig;
import com.intel.soak.plugin.constants.PluginConstants;
import org.apache.commons.io.FileUtils;

import java.io.File;

/**
 * Plugin Slave implementation for standalone mode.
 *
 * @author: Joshua Yao (yi.a.yao@intel.com)
 * @since: 12/21/13 4:11 AM
 */
public class LocalPluginSlave extends AbstractPluginSlave {

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getRuntimePluginDir() {
        return SoakConfig.Dir.RuntimePlugins.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getRuntimeResourceDir() {
        String systemTmpDir = System.getProperty("java.io.tmpdir");
        return systemTmpDir + File.separator + SoakConfig.Dir.RuntimeResources.toString();
    }

}
