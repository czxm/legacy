package com.intel.soak.plugin;

import com.intel.soak.plugin.constants.PluginConstants;
import com.intel.soak.plugin.dto.PluginInfo;
import com.intel.soak.plugin.loader.PluginInfoLoader;
import com.intel.soak.plugin.manager.IPluginManager;
import com.intel.soak.plugin.validator.IPluginValidator;
import com.intel.soak.plugin.validator.PluginValidationException;
import com.intel.soak.utils.JarFileResource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Abstract implementation of Plugin Master.
 *
 * @author Joshua Yao (yi.a.yao@intel.com)
 * @since 12/18/13 10:20 PM
 */
public abstract class AbstractPluginMaster implements PluginMaster {

    protected Log logger = LogFactory.getLog(this.getClass());

    /**
     * Plugin validators
     */
    protected List<IPluginValidator> validators;

    /**
     * Use to fetch plugin metadata.
     */
    protected PluginInfoLoader pluginInfoLoader;

    /**
     * A flag to control if SOAK should be terminated while plugin validation fails.
     */
    protected boolean exitOnError;

    /**
     * Plugin manager to manage the life-cycle of a plugin.
     */
    protected IPluginManager manager;


    private void processValidationException(Resource plugin,
                                            PluginValidationException e) {
        String msg = String.format("Plugin[%s] validation failed: %s",
                plugin.getFilename(), e.toString());
        if (exitOnError) {
            logger.error(msg);
            System.exit(1);
        } else {
            logger.warn(msg);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateDropin(final Resource dropins) {
        for (IPluginValidator validator : validators) {
            try {
                validator.doValidate(dropins);
            } catch (PluginValidationException e) {
                processValidationException(dropins, e);
            }
        }
        try {
            // Move legal dropins to plugin dir.
            FileUtils.moveFileToDirectory(dropins.getFile(),
                    new File(PluginConstants.PLUGIN_DIR), true);
        } catch (IOException e) {
            logger.error("Move plugin to plugins dir failed: " + e);
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateDropins(final Resource... dropins) {
        if (ArrayUtils.isEmpty(dropins)) return;
        for (Resource plugin : dropins) {
            validateDropin(plugin);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateDropins() {
        try {
            FileSystemResource[] rss = getFSRs(PluginConstants.DROP_INS_DIR);
            if (rss == null) return;
            validateDropins(rss);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerPlugin(final Resource plugin) {
        if (plugin instanceof JarFileResource) {
            JarFileResource jar = (JarFileResource) plugin;
            PluginInfo info = pluginInfoLoader.fetchPluginInfo(jar);
            manager.register(info);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerPlugins(final Resource... plugins) {
        if (ArrayUtils.isEmpty(plugins)) return;
        for (Resource plugin : plugins) {
            registerPlugin(plugin);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerPlugins() {
        try {
            FileSystemResource[] rss = getFSRs(PluginConstants.PLUGIN_DIR);
            registerPlugins(rss);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<PluginInfo> listPluginsInfo() {
        return this.manager.listPlugins();
    }

    private FileSystemResource[] getFSRs(String dir) throws IOException {
        File pluginDir = new File(dir);
        FileUtils.forceMkdir(pluginDir);

        File[] result = pluginDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isFile();
            }
        });
        if (result == null) return null;

        JarFileResource[] rss = new JarFileResource[result.length];
        for (int i = 0; i < result.length; i++) {
            rss[i] = new JarFileResource(new FileSystemResource(result[i]));
        }
        return rss;
    }

    /**
     * Start plugin master.
     * <p/>
     * {@link PluginMaster Plugin Master} validates plugins in drop-ins dir and
     * move legal plugins to plugins dir. Then, system registers the plugins in
     * plugins dir to {@link IPluginManager Plugin Manager}.
     */
    @Override
    public void start() {
        validateDropins();
        registerPlugins();
    }

    /**
     * Stop plugin master.
     * <p/>
     * {@link PluginMaster Plugin Master} stops all plugin workers including the
     * ones out of SOAK master JVM. e.g. Jetty server used to share plugins in
     * SOAK cluster.
     */
    @Override
    public void stop() {
        try {
            File runtimePluginDir = new File(PluginConstants.RUNTIME_PLUGIN_DIR);
            if(runtimePluginDir.exists())
                FileUtils.forceDelete(runtimePluginDir);
        } catch (Exception e) {
            throw new RuntimeException("Remove runtime plugin dir failed.", e);
        }
    }

    /**
     * Used by Spring DI only. Pls. don't set validators by any other code.
     */
    public void setValidators(List<IPluginValidator> validators) {
        this.validators = validators;
    }

    /**
     * Used by Spring DI.
     *
     * @param exitOnError true, if validation failed, exit SOAK.
     *                    false, skip the plugin.
     */
    public void setExitOnError(boolean exitOnError) {
        this.exitOnError = exitOnError;
    }

    /**
     * Used by Spring DI.
     *
     * @param manager
     */
    public void setManager(IPluginManager manager) {
        this.manager = manager;
    }

    /**
     * Used by Spring DI.
     */
    public void setPluginInfoLoader(PluginInfoLoader pluginInfoLoader) {
        this.pluginInfoLoader = pluginInfoLoader;
    }

}
