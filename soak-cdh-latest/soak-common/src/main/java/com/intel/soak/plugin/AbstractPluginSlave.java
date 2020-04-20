package com.intel.soak.plugin;

import com.intel.soak.plugin.dto.PluginInfo;
import com.intel.soak.plugin.loader.PluginFetcher;
import com.intel.soak.utils.SpringBeanFactoryManager;
import com.intel.soak.utils.JarFileResource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

/**
 * @author: Joshua Yao (yi.a.yao@intel.com)
 * @since: 12/21/13 2:41 AM
 */
public abstract class AbstractPluginSlave implements PluginSlave {

    protected String jobId;
    protected String classpath;

    protected PluginInfo[] masterPlugins;
    protected PluginInfo[] localPlugins;
    protected File jobPluginDir;

    protected PluginFetcher pluginFetcher;

    protected Log logger = LogFactory.getLog(this.getClass());

    /**
     * Fetch runtime plugin dir
     *
     * @return runtime plugin dir
     */
    protected abstract String getRuntimePluginDir();

    /**
     * Make dir for runtime-plugin dir.
     */
    protected void init() {
        try {
            File pluginDir = new File(getRuntimePluginDir());
            FileUtils.forceMkdir(pluginDir);
        } catch (IOException e) {
            throw new RuntimeException("Initial plugin slave system failed.", e);
        }
    }

    /**
     * Progress {@link ClassLoader} and {@link ApplicationContext} for each job.
     *
     * @throws MalformedURLException
     */
    protected void doLoad() throws MalformedURLException {
        ApplicationContext appCxt = PluginAppCxtProvider.get(this.localPlugins, this.classpath);
        SpringBeanFactoryManager.register(this.jobId, appCxt);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void load(String jobId, PluginInfo... pluginMetadata) {
        load(jobId, null, pluginMetadata);
    }

    /**
     * Make dir for each SOAK job in runtime-plugin dir.
     *
     * @throws IOException IO issue while making dir.
     */
    protected void createPluginDirForJob() throws IOException {
        File jobPluginDir = new File(getRuntimePluginDir(), jobId);
        Assert.isTrue(!jobPluginDir.exists(),
                String.format("Duplicated job plugin dir found, job id: %s, dir: %s",
                        jobId, jobPluginDir.getCanonicalPath()));

        FileUtils.forceMkdir(jobPluginDir);
        this.jobPluginDir = jobPluginDir;
    }

    /**
     * Fetch runtime resource dir
     *
     * @return runtime resource dir
     */
    protected abstract String getRuntimeResourceDir();

    /**
     * {@inheritDoc}
     *
     * @param jobId job id, user specified by user in load config.
     * @return
     */
    public String getJobRuntimeResourceDir(String jobId) {
        Assert.hasText(jobId, "job id should not be empty.");
        return getRuntimeResourceDir() + File.separator + jobId;
    }

    /**
     * Unjar local plugin binaries to tmp dir to provide {@link com.intel.soak.driver.IDriver}
     * and {@link com.intel.soak.transaction.Transaction} with resources in it.
     */
    protected void processPluginResources(String jobId, List<PluginInfo> plugins)
            throws IOException {

        String runtimeResourceDir = getJobRuntimeResourceDir(jobId);
        File dir = new File(runtimeResourceDir);
        if (!dir.exists()) {
            dir.mkdirs();
        } else {
            logger.warn("Plugin runtime resource dir is existing: " + runtimeResourceDir);
            return;
        }
        for (PluginInfo plugin : plugins) {
            String path = plugin.getPath();
            if (StringUtils.isEmpty(path)) {
                logger.warn("Path not found in plugin: " + plugin.getName());
                continue;
            }
            JarFileResource jar = new JarFileResource(plugin.getPath());
            jar.unjar(dir);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void load(String jobId, String classPath, PluginInfo... pluginMetadata) {

        this.jobId = jobId;
        this.classpath = classPath;
        this.masterPlugins = pluginMetadata;

        try {
            createPluginDirForJob();
            List<PluginInfo> rs = pluginFetcher.fetch(this.jobPluginDir, pluginMetadata);
            this.localPlugins = rs.toArray(new PluginInfo[rs.size()]);
            doLoad();
            processPluginResources(jobId, rs);
        } catch (Throwable e) {
            logger.error("Load plugins for job failed: job id = " + jobId, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void destroy(String jobId) {
        try {
            SpringBeanFactoryManager.destroy(jobId);

            File pluginDir = new File(new File(getRuntimePluginDir()), jobId);
            if (pluginDir.exists()) com.intel.soak.utils.FileUtils.deleteFolderAndContents(pluginDir);

            File rsDir = new File(new File(getRuntimeResourceDir()), jobId);
            if (rsDir.exists()) com.intel.soak.utils.FileUtils.deleteFolderAndContents(rsDir);

        } catch (Throwable e) {
            throw new RuntimeException("Destroy job related pluign resource failed: " + jobId, e);
        }
    }

    protected void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                logger.info("SOAK shutdown detected. Cleaning plugin runtime dir...");
                try {
                    cleanRuntimeDir();
                } catch (Throwable e) {
                    logger.error(String.format("Clean plugin runtime dir failed. Pls. remove the following dirs manually: %s, %s",
                    getRuntimePluginDir(), getRuntimeResourceDir()));
                }
            }
        });
    }

    protected void cleanRuntimeDir() {
        File runtimePluginDir = new File(getRuntimePluginDir());
        if (runtimePluginDir.exists())
            com.intel.soak.utils.FileUtils.deleteFolderAndContents(runtimePluginDir);
        File runtimePluginResDir = new File(getRuntimeResourceDir());
        if (runtimePluginResDir.exists())
            com.intel.soak.utils.FileUtils.deleteFolderAndContents(runtimePluginResDir);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void start() {
        init();
        registerShutdownHook();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void stop() {
        try {
            SpringBeanFactoryManager.destroyAll();
            cleanRuntimeDir();
        } catch (Throwable e) {
            throw new RuntimeException("Stop or clean plugin system failed.", e);
        }
    }

    /**
     * Used by Spring DI
     */
    public void setPluginFetcher(PluginFetcher pluginFetcher) {
        this.pluginFetcher = pluginFetcher;
    }
}
