package com.intel.soak.plugin.loader;

import com.intel.soak.plugin.dto.PluginInfo;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Implementation of plugin fetcher at distributed mode.
 *
 * @see PluginFetcher
 *
 * @author: Joshua Yao (yi.a.yao@intel.com)
 * @since: 12/21/13 2:45 AM
 */
public class RemotePluginFetcher implements PluginFetcher {

    private ExecutorService pool = Executors.newCachedThreadPool();

    protected static final Logger LOG = LoggerFactory.getLogger(RemotePluginFetcher.class);

    /**
     * Plugin downloader thread.
     * <p>
     * The thread downloads plugin jar from plugin shared pool.
     *
     * TODO: proxy is not supported!
     */
    public static class HTTPFileDownloader implements Callable<Boolean> {

        private PluginInfo info;
        private File localDir;

        public HTTPFileDownloader(PluginInfo info, File localDir) {
            this.info = info;
            this.localDir = localDir;
        }

        @Override
        public Boolean call() throws Exception {
            try {
                URL url = info.getRemotePath();
                if (url == null) {
                    LOG.error("Remote path not found in plugin metadata:" + info.getName());
                    return false;
                }
                LOG.info(String.format("Downloading remote resource: %s",
                        info.getRemotePath().toURI().toString()));
                File destJar = new File(localDir, getFileNameByURL(info));
                return downloadWithRetry(destJar, url, DOWNLOAD_MAX_RETRIES);
            } catch (Throwable e) {
                LOG.error("Download plugin failed: " + info.getName());
                e.printStackTrace();
                return false;
            }
        }


        private boolean downloadWithRetry(File destFile, URL remoteRes, int retries) {
            int i = 0;
            while (i++ < retries && !download(destFile, remoteRes)) {
            }
            if (i > retries) {
                LOG.error(String.format(
                        "Download remote resource failed after %d retires: %s",
                        retries, remoteRes));
                return false;
            }
            return true;
        }

        private boolean download(File destFile, URL remoteRes) {
            try {
                if (!localDir.exists() || !localDir.isDirectory()) {
                    localDir.mkdirs();
                }
                if (destFile.exists()) {
                    LOG.warn(String.format("The plugin bundle [%s] is already existing. Skip downloading...",
                            destFile.getName()));
                } else {
                    FileUtils.copyURLToFile(remoteRes, destFile);
                }
                return destFile.exists();
            } catch (Throwable e) {
                LOG.warn("Download remote resource failed, retry to fetch: " + remoteRes, e);
                return false;
            }
        }
    }

    private static synchronized String getFileNameByURL(PluginInfo pluginInfo)
            throws URISyntaxException {
        String url = pluginInfo.getRemotePath().toURI().toString();
        String[] urlElements = url.split("/");
        return urlElements[urlElements.length - 1];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PluginInfo> fetch(final File localDir,
                                  final PluginInfo... remotePlugins) {
        try {
            List<Future<Boolean>> results = new ArrayList<Future<Boolean>>();
            for (PluginInfo remotePlugin : remotePlugins) {
                String protocol = remotePlugin.getRemotePath().getProtocol();
                if (!"http".equalsIgnoreCase(protocol)) {
                    throw new RuntimeException(
                            "Remote plugin is not http protocol, plugin name: " +
                                    remotePlugin.getName());
                }
                results.add(pool.submit(new HTTPFileDownloader(remotePlugin, localDir)));
            }
            while (true) {
                int i = 0;
                for (Future<Boolean> result : results) {
                    if (result.isDone() && !result.get()) {
                        throw new RuntimeException("Download plugins failed.");
                    } else if (result.isDone() && result.get()) {
                        ++i;
                    }
                }
                if (i == results.size()) break;
                TimeUnit.SECONDS.sleep(1);
            }
            List<PluginInfo> returns = new ArrayList<PluginInfo>();
            for (PluginInfo remote : remotePlugins) {
                PluginInfo local = (PluginInfo) BeanUtils.cloneBean(remote);
                local.setPath(new File(localDir, getFileNameByURL(remote)).getCanonicalPath());
                returns.add(local);
            }
            LOG.info(String.format("%d plugins have been downloaded successfully!", returns.size()));
            return returns;
        } catch (Throwable e) {
            FileUtils.deleteQuietly(localDir);
            throw new RuntimeException("Download plugins failed.", e);
        }
    }

}
