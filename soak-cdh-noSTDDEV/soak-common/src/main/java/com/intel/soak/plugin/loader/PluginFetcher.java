package com.intel.soak.plugin.loader;

import com.intel.soak.plugin.dto.PluginInfo;

import java.io.File;
import java.util.List;

/**
 * Fetch plugin jars from plugin shared pool.
 * <p>
 * System creates a local dir for each running job at first. Then, at standalone mode,
 * the job related plugin jars will be copied to the dir via local file system. At
 * distributed mode, jars will be downloaded via HTTP protocol. The plugin shared pool
 * is a jetty file server used to share plugin jars in cluster.
 *
 * @author Joshua Yao (yi.a.yao@intel.com)
 * @since 12/19/13 3:48 AM
 */
public interface PluginFetcher {

    int timeout = 300;
    int DOWNLOAD_MAX_RETRIES = 3;

    /**
     * Fetch plugins and persist locally.
     *
     * @param localDir      local plugin dir for a job
     * @param plugins       plugins metadata registered in {@link com.intel.soak.plugin.manager.IPluginManager}
     * @return              local plugins metadata
     */
    List<PluginInfo> fetch(final File localDir, final PluginInfo... plugins);

}
