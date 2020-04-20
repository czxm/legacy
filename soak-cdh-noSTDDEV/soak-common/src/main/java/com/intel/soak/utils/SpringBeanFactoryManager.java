package com.intel.soak.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: joshua
 * Date: 10/28/13
 * Time: 10:14 PM
 * To change this template use File | Settings | File Templates.
 */
public enum SpringBeanFactoryManager {

    INSTANCE;

    private static final transient Map<String, ApplicationContext> BEAN_FACTORIES = new ConcurrentHashMap<String, ApplicationContext>();
    private static final Set<String> DEFAULT_CONF_SET = new HashSet<String>();

    private static final Log LOG = LogFactory.getLog(SpringBeanFactoryManager.class);

    public static final String DEFAULT_FACTORY_NAME = "system";

    static {
        DEFAULT_CONF_SET.add("spring-soak-common.xml");
    }

    public static synchronized void init(Set<String> springConfFiles) {
        DEFAULT_CONF_SET.addAll(springConfFiles);
        init();
    }

    public static synchronized void init() {
        BEAN_FACTORIES.put(DEFAULT_FACTORY_NAME,
                new ClassPathXmlApplicationContext(
                        DEFAULT_CONF_SET.toArray(new String[DEFAULT_CONF_SET.size()])));
        LOG.info("Initial spring application contexts for core.");
    }

    public static synchronized void destroy(String jobId) {
        ApplicationContext appCxt = BEAN_FACTORIES.get(jobId);
        if (appCxt instanceof AbstractApplicationContext) {
            ((AbstractApplicationContext) appCxt).close();
        }
        BEAN_FACTORIES.remove(jobId);
    }

    public static synchronized void destroyAll() {
        Collection<ApplicationContext> appCxtList = BEAN_FACTORIES.values();
        for (ApplicationContext appCxt : appCxtList) {
            if (appCxt instanceof AbstractApplicationContext) {
                ((AbstractApplicationContext) appCxt).close();
            }
        }
        BEAN_FACTORIES.clear();
    }

    public static synchronized ApplicationContext getSystemAppCxt() {
        return BEAN_FACTORIES.get(DEFAULT_FACTORY_NAME);
    }

    public static synchronized ApplicationContext getSystemAppCxtCopy() {
          return new ClassPathXmlApplicationContext(new String[]{}, getSystemAppCxt());
    }

    public static synchronized boolean register(final String name, final ApplicationContext context) {
        if (StringUtils.isEmpty(name)) {
            LOG.warn(String.format("Empty spring context name provided."));
            return false;
        }
        if (BEAN_FACTORIES.get(name) != null) {
            LOG.warn(String.format("The spring context you registered has been existing: %s", name));
            return false;
        }
        BEAN_FACTORIES.put(name, context);
        return true;
    }

    public static synchronized boolean reRegister(final String name, final ApplicationContext context) {
        if (StringUtils.isEmpty(name)) {
            LOG.warn(String.format("Empty spring context name provided."));
            return false;
        }
        BEAN_FACTORIES.put(name, context);
        return true;
    }

    public static synchronized ApplicationContext getAppCxtByName(String name) {
        return BEAN_FACTORIES.get(name);
    }

}


