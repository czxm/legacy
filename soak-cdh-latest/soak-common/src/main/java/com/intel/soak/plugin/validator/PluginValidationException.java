package com.intel.soak.plugin.validator;

import org.springframework.core.io.Resource;

/**
 * Plugin validation exception.
 *
 * @author: Joshua Yao (yi.a.yao@intel.com)
 * @since: 12/19/13 1:22 AM
 */
public class PluginValidationException extends Exception {

    public PluginValidationException(String msg) {
        super(msg);
    }

    public PluginValidationException(String msg, Throwable e) {
        super(msg, e);
    }

    public PluginValidationException(String msg, Resource plugin,
                                     IPluginValidator validator) {
        super(String.format(
                "Validate plugin[%s] with validator[%s] failed: %s ",
                plugin.getFilename(), validator.getClass(), msg));
    }

}
