package com.intel.soak.plugin.loader;

import com.intel.soak.plugin.dto.PluginInfo;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.util.Assert;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of plugin fetcher at standalone mode.
 *
 * @see PluginFetcher
 *
 * @author: Joshua Yao (yi.a.yao@intel.com)
 * @since: 12/21/13 2:45 AM
 */
public class LocalPluginFetcher implements PluginFetcher {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PluginInfo> fetch(File localDir, PluginInfo... plugins) {
        List<PluginInfo> result = new ArrayList<PluginInfo>();
        try {
            for (PluginInfo plugin : plugins) {
                File pluginBinary = new File(plugin.getPath());
                Assert.isTrue(pluginBinary.exists() && pluginBinary.canRead(),
                        String.format("Binary[%s] does not exist or is not readable.",
                                plugin.getPath()));

                FileUtils.copyFileToDirectory(pluginBinary, localDir);
                PluginInfo pi = (PluginInfo) BeanUtils.cloneBean(plugin);
                pi.setPath(new File(localDir, pluginBinary.getName()).getCanonicalPath());
                result.add(pi);
            }
            return result;
        } catch (Throwable e) {
            FileUtils.deleteQuietly(localDir);
            throw new RuntimeException("Download plugins failed.", e);
        }
    }

}
