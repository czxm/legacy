package com.intel.soak.plugin;

import java.io.File;
import java.io.IOException;

import com.intel.soak.config.SoakConfig;
import org.apache.commons.io.FileUtils;
import org.springframework.util.Assert;

import com.intel.soak.plugin.constants.PluginConstants;

/**
 * Plugin slave implementation for distributed mode.
 *
 * @author: Joshua Yao (yi.a.yao@intel.com)
 * @since: 12/21/13 4:12 AM
 */
public class ClusterPluginSlave extends AbstractPluginSlave {

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getRuntimePluginDir() {
        return SoakConfig.Dir.AgentRuntimePlugins.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getRuntimeResourceDir() {
        String systemTmpDir = System.getProperty("java.io.tmpdir");
        return systemTmpDir + File.separator + SoakConfig.Dir.AgentRuntimeResources.toString();
    }

}
