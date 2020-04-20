package com.intel.soak.plugin;

import com.intel.soak.plugin.dto.PluginInfo;
import com.intel.soak.utils.SpringBeanFactoryManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.UrlResource;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: joshua
 * Date: 10/28/13
 * Time: 10:40 PM
 * To change this template use File | Settings | File Templates.
 */
public enum PluginAppCxtProvider {

    INSTANCE;

    private static Log LOG = LogFactory.getLog(PluginAppCxtProvider.class);
    private static final String DEFAULT_CP_SEPARATOR = System.getProperty("path.separator");


    private static void processExtraClassPath(List<URL> currentClassPath, String extraClassPath)
            throws MalformedURLException {

        String[] extraCPs = extraClassPath.split(DEFAULT_CP_SEPARATOR);
        for (String extraCP : extraCPs) {
            File cp = new File(extraCP);
            if (!cp.exists()) {
                LOG.warn("The file in your classpath does not exist: " + extraCP);
                continue;
            }
            if (cp.isFile()) {
                currentClassPath.add(cp.toURI().toURL());
            } else {
                File[] files = cp.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.isFile() && file.getName().endsWith(".jar");
                    }
                });
                for (File file : files) {
                    currentClassPath.add(file.toURI().toURL());
                }
                currentClassPath.add(cp.toURI().toURL());
            }
        }
    }

    private static ClassLoader getClassLoader(final PluginInfo[] plugins, String extraClassPath)
            throws MalformedURLException {

        List<URL> pluginJarUrls = new LinkedList<URL>();
        for (PluginInfo plugin : plugins) {
            try {
                URL pluginURL = new File(plugin.getPath()).toURI().toURL();
                pluginJarUrls.add(pluginURL);
            } catch (MalformedURLException e) {
                LOG.warn("Incorrect plugin URL found: " + e.getMessage());
                LOG.warn(String.format("Skip loading plugin [%s].", plugin.getName()));
                continue;
            }
        }
        if (!StringUtils.isEmpty(extraClassPath))
            processExtraClassPath(pluginJarUrls, extraClassPath);

        URL[] cpUrls = pluginJarUrls.toArray(new URL[pluginJarUrls.size()]);
        URLClassLoader classLoader = new URLClassLoader(cpUrls);

        LOG.info("Job CLASSPATH:" + Arrays.toString(classLoader.getURLs()));

        return classLoader;
    }

    public static synchronized ApplicationContext get(final PluginInfo[] plugins, final String extraClassPath)
            throws MalformedURLException {

        LOG.info("Registering plugins to Spring IOC Container...");

        ApplicationContext appCxt = SpringBeanFactoryManager.getSystemAppCxtCopy();
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory)
                appCxt.getAutowireCapableBeanFactory();
        beanFactory.setBeanClassLoader(getClassLoader(plugins, extraClassPath));

        for (PluginInfo plugin : plugins) {

            String conf = String.format("jar:file:%s!/%s-plugin.xml",
                    plugin.getPath(), plugin.getName());

            UrlResource confRes = null;
            try {
                confRes = new UrlResource(new URL(conf));
            } catch (MalformedURLException e) {
                LOG.warn("Incorrect plugin config URL found: " + e.getMessage());
                LOG.warn(String.format("Skip loading plugin [%s].", plugin.getName()));
                continue;
            }

            XmlBeanFactory xbf = new XmlBeanFactory(confRes);
            String[] beanIds = xbf.getBeanDefinitionNames();
            for (String beanId : beanIds) {
                BeanDefinition bd = xbf.getMergedBeanDefinition(beanId);
                beanFactory.registerBeanDefinition(beanId, bd);
            }
        }
        return appCxt;
    }

//    public static synchronized ApplicationContext get(Collection<PluginInfo> plugins, String extraClassPath) throws MalformedURLException {
//        LOG.info("Loading all plugins to Spring IOC Container...");
//
//        ApplicationContext appCxt = SpringBeanFactoryManager.getSystemAppCxtCopy();
//        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) appCxt.getAutowireCapableBeanFactory();
//
//        for (PluginInfo plugin : plugins) {
//            String conf = String.format("jar:file:%s!/%s-plugin.xml", plugin.getPath(), plugin.getName());
//            UrlResource confRes = null;
//            try {
//                confRes = new UrlResource(new URL(conf));
//            } catch (MalformedURLException e) {
//                LOG.warn("Incorrect plugin config URL found: " + e.getMessage());
//                LOG.warn(String.format("Skip loading plugin [%s].", plugin.getName()));
//                continue;
//            }
//            XmlBeanFactory xbf = new XmlBeanFactory(confRes);
//            String[] beanIds = xbf.getBeanDefinitionNames();
//            for (String beanId : beanIds) {
//                BeanDefinition bd = xbf.getMergedBeanDefinition(beanId);
//                beanFactory.registerBeanDefinition(beanId, bd);
//                PluginComponent comp = new PluginComponent(beanId, plugin);
//                plugin.addComponent(comp);
//            }
//        }
//        beanFactory.setBeanClassLoader(getClassLoader(plugins, extraClassPath));
//
//        registerPlugin(appCxt, plugins);
//        return appCxt;
//    }
//
//    private static void registerPlugin(ApplicationContext appCxt, Collection<PluginInfo> plugins) {
//        for (PluginInfo plugin : plugins) {
//            List<PluginComponent> realComponents = new ArrayList<PluginComponent>();
//            for (PluginComponent comp : plugin.getComponents().values()) {
//                String id = comp.getId();
//                Object obj = appCxt.getBean(id);
//                if (!(obj instanceof Pluggable)) {
//                    LOG.warn(String.format("Bean [%s] is not pluggable, skip registering it.", id));
//                    continue;
//                }
//                Plugin annotation = obj.getClass().getAnnotation(Plugin.class);
//                if (annotation == null) {
//                    LOG.warn(String.format("Annotation plugin not found in bean [%s], skip registering it.", id));
//                    continue;
//                }
//                comp.setType(annotation.type());
//                comp.setDesc(annotation.desc());
//                comp.setName(id + "-" + obj.getClass().getSimpleName());
//                realComponents.add(comp);
//            }
//            plugin.setComponents(realComponents);
//            PluginFSM.INSTANCE.stateChange(plugin, PluginFSM.PLUGIN_OPTS.REGISTER, true);
//        }
//    }
//
//    private static final String DEFAULT_CP_SEPARATOR = System.getProperty("path.separator");
//
//    private static void processExtraClassPath(List<URL> currentClassPath, String extraClassPath)
//            throws MalformedURLException {
//
//        String[] extraCPs = extraClassPath.split(DEFAULT_CP_SEPARATOR);
//        for (String extraCP : extraCPs) {
//            File cp = new File(extraCP);
//            if (!cp.exists()) {
//                LOG.warn("The file in your classpath does not exist: " + extraCP);
//                continue;
//            }
//            if (cp.isFile()) {
//                currentClassPath.add(cp.toURI().toURL());
//            } else {
//                File[] files = cp.listFiles(new FileFilter() {
//                    @Override
//                    public boolean accept(File file) {
//                        return file.isFile();
//                    }
//                });
//                for (File file : files) {
//                    currentClassPath.add(file.toURI().toURL());
//                }
//            }
//        }
//    }
//
//    private static ClassLoader getClassLoader(final Collection<PluginInfo> plugins, String extraClassPath)
//            throws MalformedURLException {
//
//        List<URL> pluginJarUrls = new LinkedList<URL>();
//        for (PluginInfo plugin : plugins) {
//            try {
//                pluginJarUrls.add(new File(plugin.getPath()).toURI().toURL());
//            } catch (MalformedURLException e) {
//                LOG.warn("Incorrect plugin URL found: " + e.getMessage());
//                LOG.warn(String.format("Skip loading plugin [%s].", plugin.getName()));
//                continue;
//            }
//        }
//        if (!StringUtils.isEmpty(extraClassPath))
//            processExtraClassPath(pluginJarUrls, extraClassPath);
//        return new URLClassLoader(pluginJarUrls.toArray(new URL[pluginJarUrls.size()]));
//    }

}
