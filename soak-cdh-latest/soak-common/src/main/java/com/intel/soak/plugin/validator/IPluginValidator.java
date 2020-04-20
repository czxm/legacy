package com.intel.soak.plugin.validator;

import org.springframework.core.io.Resource;

/**
 * Common interface of plugin validator. All plugin validators must implement
 * the interface.
 *
 * @author  Joshua Yao (yi.a.yao@intel.com)
 * @since 12/18/13
 */
public interface IPluginValidator {

    /**
     * Validate the plugin.
     *
     * @param plugin                        plugin to be validated.
     * @throws PluginValidationException    if validation failed, throw it.
     */
	void doValidate(final Resource plugin) throws PluginValidationException;

}
