package com.intel.soak.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.intel.soak.MetricsData.Aggregator;;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Metrics {
	
	String name();
	
	Aggregator[] aggregators() default {Aggregator.MED};

}
