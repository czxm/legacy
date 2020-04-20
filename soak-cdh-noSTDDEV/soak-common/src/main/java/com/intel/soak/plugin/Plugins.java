package com.intel.soak.plugin;

import com.intel.soak.driver.IDriver;
import com.intel.soak.plugin.dto.PluginComponent;
import com.intel.soak.plugin.dto.PluginInfo;
import com.intel.soak.utils.SpringBeanFactoryManager;
import com.intel.soak.transaction.Transaction;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Plugin APIs.
 * <p/>
 * {@link Plugins} provides user with a common interface to manage and fetch plugins.
 *
 * @author: Joshua Yao (yi.a.yao@intel.com)
 * @since: 12/22/13 5:20 AM
 */
public class Plugins {

    private PluginMaster pluginMaster;
    private PluginSlave pluginSlave;

    private final Log LOG = LogFactory.getLog(Plugins.class);

    /**
     * Fetch instance of {@link IDriver} by jobId and driver name.
     *
     * @param jobId      identify of a job, the name of load config.
     * @param driverName spring bean id of the driver you wanna fetch.
     * @return driver instance
     */
    public IDriver getDriver(String jobId, String driverName) {
        return this.<IDriver>getObjByAppCxt(jobId, driverName, IDriver.class);
    }

    /**
     * Fetch instance of {@link Transaction} by jobId and transaction name.
     *
     * @param jobId    identify of a job, the name of load config.
     * @param tranName spring bean id of the transaction you wanna fetch.
     * @return transaction instance
     */
    public Transaction getTransaction(String jobId, String tranName) {
        return this.<Transaction>getObjByAppCxt(jobId, tranName, Transaction.class);
    }

    /**
     * Fetch spring bean by job id and plugin bean id.
     *
     * @param jobId identify of a job, the name of load config.
     * @param name  spring bean id
     * @return object instance
     */
    public Object getPluginObj(String jobId, String name) {
        return this.<Object>getObjByAppCxt(jobId, name, Object.class);
    }

    /**
     * Fetch instance of {@link Pluggable} by job id and plugin component name.
     *
     * @param jobId identify of a job, the name of load config.
     * @param name  spring bean id of the plugin component you wanna fetch.
     * @return pluggable instance
     */
    public Pluggable getPluggableObj(String jobId, String name) {
        return this.<Pluggable>getObjByAppCxt(jobId, name, Pluggable.class);
    }

    /**
     * Fetch 'system' beans which are used by SOAK framework. Note that you are not
     * able to fetch any plugin beans via this method. Plugin beans are isolated by
     * plugin system via different {@link ClassLoader}.
     *
     * @param name spring bean id
     * @return 'system' beans
     */
    public Object getSystemObj(String name) {
        return this.getPluginObj("system", name);
    }

    /**
     * A generic approach to fetch beans in plugin system.
     *
     * @param jobId identify of a job, the name of load config.
     * @param name  spring bean id.
     * @param clazz class of the object you wanna fetch
     * @param <T>   class of the object you wanna fetch
     * @return spring bean
     */
    public <T> T getObjByAppCxt(String jobId, String name, Class<T> clazz) {

        Assert.isTrue(!StringUtils.isEmpty(jobId)
                && !StringUtils.isEmpty(name) && clazz != null,
                "Job id or bean name or bean class can not be empty!");

        ApplicationContext appCxt = SpringBeanFactoryManager.getAppCxtByName(jobId);
        if (appCxt == null)
            throw new RuntimeException(String.format(
                    "Application context is not found, job id = %s", jobId));

        Object obj = appCxt.getBean(name);
        if (obj == null)
            throw new RuntimeException(String.format(
                    "Bean %s is not found", name));

        try {
            return clazz.cast(obj);
        } catch (ClassCastException e) {
            throw new RuntimeException(String.format(
                    "Bean %s is not a %s", name, clazz.getName()));
        }
    }

    /**
     * This method is used to create a new {@link ClassLoader} for a job and create a
     * {@link ApplicationContext} based on the ClassLoader. In other words, 1 job 1
     * ClassLoader and 1 job 1 ApplicationContext. This mechanism is used to ensure the
     * isolation of plugins and their referenced jars. All the job related plugin classes
     * including referenced classes in 3rd party jars will be delegated to Spring IOC to
     * maintain the object references.
     *
     * @param jobId   identify of a job, the name of load config.
     * @param plugins plugins info. of the job
     */
    public void loadAndRegisterPlugins(String jobId, List<PluginInfo> plugins) {
        loadAndRegisterPlugins(jobId, plugins, null);
    }

    /**
     * This method is used to create a new {@link ClassLoader} for a job and create a
     * {@link ApplicationContext} based on the ClassLoader. In other words, 1 job 1
     * ClassLoader and 1 job 1 ApplicationContext. This mechanism is used to ensure the
     * isolation of plugins and their referenced jars. All the job related plugin classes
     * including referenced classes in 3rd party jars will be delegated to Spring IOC to
     * maintain the object references.
     *
     * @param jobId     identify of a job, the name of load config.
     * @param plugins   plugins info. of the job
     * @param classPath extra classpath, used to specify plugins referenced 3rd party
     *                  jars. Its parent classpath is the SOAK classpath.
     */
    public void loadAndRegisterPlugins(String jobId, List<PluginInfo> plugins,
                                       String classPath) {
        try {
            pluginSlave.load(jobId, classPath,  //TODO: array to list
                    plugins.toArray(new PluginInfo[plugins.size()]));
        } catch (Throwable e) {
            throw new RuntimeException("Load and register plugins for job " + jobId + "failed.", e);
        }
    }

    /**
     * Fetch the plugins info via plugin component ids. At distributed mode, the method is
     * invalid when invoked at slave/agent side.
     *
     * @param componentIds
     * @return
     */
    public List<PluginInfo> getPluginsInfoByComponentIds(List<String> componentIds) {
        Collection<PluginInfo> pluginsInfo = pluginMaster.listPluginsInfo();
        List<PluginInfo> result = new ArrayList<PluginInfo>();
        for (String componentId : componentIds) {
            for (PluginInfo pluginInfo : pluginsInfo) {
                PluginComponent pc = pluginInfo.getComponent(componentId);
                if (pc != null) {
                    if (!result.contains(pc.getPlugin())) {
                        result.add(pc.getPlugin());
                    }
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Destroy the specified job related {@link org.springframework.context.ApplicationContext}
     * and runtime dirs.
     *
     * @param jobId job id, user specified by user in load config.
     */
    public void destroy(String jobId) {
        pluginSlave.destroy(jobId);
    }

    /**
     * Fetch job runtime resource dir.
     *
     * @param jobId     job id, user specified by user in load config.
     * @return          job runtime resource dir
     */
    public String getJobRuntimeResourceDir(String jobId) {
        return pluginSlave.getJobRuntimeResourceDir(jobId);
    }

    public void setPluginMaster(PluginMaster pluginMaster) {
        this.pluginMaster = pluginMaster;
    }

    public void setPluginSlave(PluginSlave pluginSlave) {
        this.pluginSlave = pluginSlave;
    }
}
