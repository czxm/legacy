package com.intel.soak.plugin.validator;

import com.intel.soak.plugin.constants.PluginConstants;
import com.intel.soak.utils.JarFileResource;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.Resource;

import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Validate the plugin MANIFEST. Plugin name is required.
 *
 * @author: Joshua Yao (yi.a.yao@intel.com)
 * @since: 12/19/13 2:01 AM
 */
public class PluginManifestValidator implements IPluginValidator {

    @Override
    public void doValidate(Resource plugin) throws PluginValidationException {
        try {
            JarFileResource jar = new JarFileResource(plugin);
            Manifest manifest = jar.getManifest();
            Attributes attributes = manifest.getMainAttributes();
            String name = attributes.getValue(PluginConstants.PLUGIN_NAME_KEY);
            if (StringUtils.isEmpty(name)) {
                throw new PluginValidationException("plugin name not found!", plugin, this);
            }
        } catch (PluginValidationException e) {
            throw e;
        } catch (Throwable e) {
            throw new PluginValidationException("Unexpected validation error!", e);
        }
    }

}
