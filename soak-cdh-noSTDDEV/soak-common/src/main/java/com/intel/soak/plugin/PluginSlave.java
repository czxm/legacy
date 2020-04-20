package com.intel.soak.plugin;

import com.intel.soak.Bootable;
import com.intel.soak.plugin.dto.PluginInfo;

/**
 * The abstraction of plugin slave which is used to load & destroy plugins for SOAK jobs.
 * <p/>
 * {@link PluginSlave} use Spring to maintain the references of plugin classes. It creates an
 * independent {@link ClassLoader} and {@link org.springframework.context.ApplicationContext} for each job.
 * The mechanism ensure class isolation at job-level to avoid the jar conflicts. Also, it provides several
 * extra benefits. E.g. it makes this possible to submit MR jobs to both MRv1 cluster and MRv2 cluster in
 * one job.
 * <p/>
 * After SOAK job finishes or terminated, slave will destroy
 * {@link org.springframework.context.ApplicationContext} to recycle system resources.
 *
 * @see ClassLoader
 * @see org.springframework.context.ApplicationContext
 *
 * @author Joshua Yao (yi.a.yao@intel.com)
 * @since 12/18/13
 */
public interface PluginSlave extends Bootable {

    /**
     * Create an independent {@link ClassLoader} and {@link org.springframework.context.ApplicationContext}
     * for each job to load plugin classes and delegate them to Spring IOC container.
     *
     * @param jobId                 job id, user specified by user in load config.
     * @param pluginMetadata        plugin meta info., the plugins used in the SOAK job.
     */
    void load(String jobId, PluginInfo... pluginMetadata);

    /**
     * Create an independent ${@link ClassLoader} and {@link org.springframework.context.ApplicationContext}
     * for each job to load plugin classes and delegate them to Spring IOC container.
     *
     * @param jobId                 job id, user specified by user in load config.
     * @param classPath             extra classpath specified by user in transaction config.
     * @param pluginMetadata        plugin meta info., the plugins used in the SOAK job.
     */
    void load(String jobId, String classPath, PluginInfo... pluginMetadata);

    /**
     * Destroy the specified job related {@link org.springframework.context.ApplicationContext} and
     * runtime dirs.
     *
     * @param jobId                 job id, user specified by user in load config.
     */
    void destroy(String jobId);

    /**
     * Fetch job runtime resource dir.
     *
     * @param jobId     job id, user specified by user in load config.
     * @return          job runtime resource dir
     */
    String getJobRuntimeResourceDir(String jobId);

}
