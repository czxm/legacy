/*
 * Copyright 2011-2013 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intel.soak.plugin.mapreduce.executor;

import com.intel.soak.plugin.mapreduce.ExecutionUtils;
import com.intel.soak.plugin.mapreduce.executor.HadoopCodeExecutor;
import org.apache.hadoop.conf.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.util.ReflectionUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

/**
 * Customized executor for Hadoop jars.
 * 
 * @author Costin Leau
 */
/**
 * Modified for soak
 */
public class JarExecutor extends HadoopCodeExecutor<Object> {

    private File savedCfg;
    private String cfgName;

    @Override
    protected ClassLoader createCLForJar(Resource jar, ClassLoader parentCL, Configuration cfg) {
        return new ClassLoader(ExecutionUtils.createParentLastClassLoader(jar, parentCL, cfg)) {
            public URL getResource(String name) {
                if (savedCfg != null && cfgName != null & cfgName.equals(name)) {
                    try {
                        return savedCfg.toURI().toURL();
                    } catch (MalformedURLException e) {
                        throw new IllegalStateException("Cannot add custom config.", e);
                    }
                }
                return super.getResource(name);
            }
        };
    }

    @Override
    protected Object resolveTargetObject(Class<Object> type) {
        // For jar, the main class does not have to be instantiated
        return null;
    }

    private List<String> defaultResources(){
        // reflection hack to remove default resource
        Field f = ReflectionUtils.findField(Configuration.class, "defaultResources");
        ReflectionUtils.makeAccessible(f);
        return (List<String>) ReflectionUtils.getField(f, null);
    }

    @Override
    protected void preExecution(Configuration cfg) {
        cfgName = "custom-cfg-for-" + jar + "_" + UUID.randomUUID();
        try {
            savedCfg = File.createTempFile("jar-cfg-", null);
            cfg.writeXml(new FileOutputStream(savedCfg));
            defaultResources().add(cfgName);
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot set custom configuration", e);
        }
    }

    @Override
    protected void postExecution(Configuration cfg) {
        defaultResources().remove(cfgName);
        savedCfg.delete();
        savedCfg = null;
        cfgName = null;
    }

    @Override
    protected Object invokeTargetObject(Configuration cfg,
            Object target, Class<Object> targetClass, String[] args) {
        Method main = ReflectionUtils.findMethod(targetClass, "main", String[].class);
        return ReflectionUtils.invokeMethod(main, null, new Object[] { args });
    }

    @Override
    public void afterPropertiesSet() throws Exception {
//        if (jar == null || !jar.exists())
//            throw new IllegalStateException("jar location [" + jar + "] not found");
    }

    public void setMainClass(String className) {
        setTargetClassName(className);
    }

}
