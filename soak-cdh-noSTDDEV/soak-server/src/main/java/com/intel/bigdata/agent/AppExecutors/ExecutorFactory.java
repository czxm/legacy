package com.intel.bigdata.agent.AppExecutors;

import java.lang.annotation.Annotation;

/**
 * Created with IntelliJ IDEA.
 * User: jzhu61
 * Date: 11/6/13
 * Time: 5:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExecutorFactory {
    private Object appmsg;
    private Annotation an;

    public ExecutorFactory(Object appmsg) {
        this.appmsg = appmsg;
        an = appmsg.getClass().getAnnotation(com.intel.bigdata.common.protocol.AppAnnotation.class);
    }

    public String getType() {
        if (an == null) {
            return null;
        } else {
            String anString = an.toString();
            String type = anString.substring(anString.lastIndexOf("type=") + 5, anString.indexOf(","));
            return type;
        }
    }

    public Class<?> getAppClass() {
        if (an == null) {
            return null;
        } else {
            String anString = an.toString();
            String appClass = anString.substring(anString.lastIndexOf("appClass=") + 9, anString.lastIndexOf(")"));
            Class<?> ae = null;
            try {
                ae = Class.forName(appClass);
            } catch (ClassNotFoundException e) {
                return null;
            }
            return ae;
        }
    }

    public String getActorName() {
        if (an == null) {
            return null;
        } else {
            String anString = an.toString();
            String appActor = anString.substring(anString.lastIndexOf("appActor=") + 9, anString.lastIndexOf(","));
            return appActor;
        }
    }
}
