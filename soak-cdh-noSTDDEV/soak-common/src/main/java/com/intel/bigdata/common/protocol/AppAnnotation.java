package com.intel.bigdata.common.protocol;

import java.lang.annotation.*;

/**
 * Created with IntelliJ IDEA.
 * User: jzhu61
 * Date: 11/7/13
 * Time: 9:11 AM
 * To change this template use File | Settings | File Templates.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AppAnnotation {
    ExecType type();
    String appActor();
    String appClass();
}
