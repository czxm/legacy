package com.intel.soak.plugin.loader;

import com.intel.soak.plugin.annotation.PLUGIN_TYPE;
import com.intel.soak.plugin.annotation.Plugin;
import com.intel.soak.plugin.constants.PluginConstants;
import com.intel.soak.plugin.dto.PluginComponent;
import com.intel.soak.plugin.dto.PluginInfo;
import com.intel.soak.utils.JarFileResource;
import com.intel.soak.utils.JarUtils;
import com.intel.soak.utils.JavassistAnnotationsHelper;
import javassist.bytecode.ClassFile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.UrlResource;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Generic implementation of {@link PluginInfoLoader}.
 *
 * @see PluginInfoLoader
 *
 * @author Joshua Yao (yi.a.yao@intel.com)
 * @since 12/19/13 3:48 AM
 */
public class GenericPluginInfoLoader implements PluginInfoLoader, PluginConstants {

    protected Log logger = LogFactory.getLog(this.getClass());

    private URL getPluginRemoteURL(JarFileResource localPlugin)
            throws UnknownHostException, MalformedURLException {

        String name = localPlugin.getFilename();
        //TODO: process different network env.
        String hostName = InetAddress.getLocalHost().getCanonicalHostName();

        //TODO: hard code the port.
        return new URL(PLUGIN_SHARE_PROTOCOL, hostName,
                PLUGIN_SHARE_PORT_DEFAULT, "/" + name);
    }

    /**
     * Fetch plugin component metadata via Javassist.
     *
     * @param pc                plugin component metadata
     * @param pluginPath        plugin path
     * @throws IOException      io issues occurs while reading plugin jar
     */
    protected void fetchPluginComponentInfo(PluginComponent pc, String pluginPath)
            throws IOException {

        URL classUrl = JarUtils.getClassURLinJar(new File(pluginPath), pc.getName());
        ClassFile cf = new ClassFile(new DataInputStream(classUrl.openStream()));

        Annotation[] anns = JavassistAnnotationsHelper.getAnnotationsForClass(cf);
        for (Annotation ann : anns) {
            if (ann instanceof Plugin) {
                Plugin pAnn = (Plugin) ann;
                pc.setType(pAnn.type());
                pc.setDesc(pAnn.desc());
                return;
            }
        }
        pc.setType(PLUGIN_TYPE.UNKNOWN);
        pc.setDesc("Unknown");
    }

    /**
     * Fetch plugin component list from spring configuration file.
     *
     * @param plugin            plugin jar resource
     * @param info              plugin metadata dto
     * @throws IOException      io issues occurs while reading plugin jar
     */
    protected void fetchPluginComps(JarFileResource plugin, PluginInfo info)
            throws IOException {

        String pluginPath = plugin.getFile().getCanonicalPath();
        String confUrl = String.format("jar:file:%s!/%s-plugin.xml",
                pluginPath, info.getName());
        UrlResource conf = new UrlResource(confUrl);

        List<BeanReader.BeanDefinition> bds = BeanReader.INSTANCE.load(conf);
        for (BeanReader.BeanDefinition bd : bds) {
            try{
                PluginComponent pc = new PluginComponent(bd.getId(), info);
                pc.setName(bd.getClazz());
                fetchPluginComponentInfo(pc, pluginPath);
                info.addComponent(pc);
            }
            catch(Exception e){
                logger.warn(String.format(
                        "Bean %s is not a pluggable component, skip.",
                        bd.getId()));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PluginInfo fetchPluginInfo(JarFileResource plugin) {
        try {
            Manifest manifest = plugin.getManifest();
            Attributes attributes = manifest.getMainAttributes();
            String name = attributes.getValue(PLUGIN_NAME_KEY);
            String version = attributes.getValue(PLUGIN_VERSION_KEY);
            String desc = attributes.getValue(PLUGIN_DESC_KEY);
            PluginInfo pi = new PluginInfo.Builder(
                    name,
                    plugin.getPath(),
                    getPluginRemoteURL(plugin))
                    .version(version)
                    .desc(desc)
                    .build();
            fetchPluginComps(plugin, pi);
            return pi;
        } catch (Exception e) {
            throw new RuntimeException("Fetch plugin info failed.", e);
        }
    }

}
