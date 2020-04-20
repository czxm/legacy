package com.intel.soak.plugin;

import com.intel.soak.plugin.Plugins;
import com.intel.soak.utils.SpringBeanFactoryManager;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.Callable;

/**
 * Created with IntelliJ IDEA.
 * User: xzhan27
 * Date: 12/25/13
 * Time: 10:13 AM
 */
public class PluginContextCallable<T> implements Callable<T> {

    private Plugins plugins;
    private String jobId;
    private Callable<T> action;

    public PluginContextCallable(String jobId, Plugins plugins, Callable<T> action){
        this.jobId = jobId;
        this.plugins = plugins;
        this.action = action;
    }

    @Override
    public T call() throws Exception {
        ApplicationContext appCxt = SpringBeanFactoryManager.getAppCxtByName(jobId);
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory)
                appCxt.getAutowireCapableBeanFactory();
        Thread.currentThread().setContextClassLoader(beanFactory.getBeanClassLoader());
        if(action != null)
            return action.call();
        return null;
    }
}
