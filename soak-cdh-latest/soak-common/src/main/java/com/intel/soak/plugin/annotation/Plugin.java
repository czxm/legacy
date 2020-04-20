package com.intel.soak.plugin.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.TYPE})
public @interface Plugin {
	
	String desc() default "unknown";
	
	PLUGIN_TYPE type() default PLUGIN_TYPE.UNKNOWN;

}
