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

import com.intel.soak.plugin.mapreduce.ConfigurationUtils;
import com.intel.soak.plugin.mapreduce.ExecutionUtils;
import com.intel.soak.plugin.mapreduce.JobGenericOptions;
import com.intel.soak.plugin.mapreduce.ParentLastURLClassLoader;
import com.intel.soak.utils.JarUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.security.PrivilegedExceptionAction;
import java.util.Properties;

import static com.intel.soak.plugin.mapreduce.MapredPluginConstants.JOB_JAR_LOCATION_KEY;

/**
 * Base configuration class for executing custom Hadoop code (such as Tool or Jar).
 * 
 * @author Costin Leau
 */
/**
 * Modified for soak
 */
public abstract class HadoopCodeExecutor<T> extends JobGenericOptions
        implements InitializingBean, BeanClassLoaderAware {

    String[] arguments;
    Properties properties;

    Configuration configuration;  // Add to Configuration.

    T target;
    String targetClassName;

    protected Resource jar;
    private ClassLoader beanClassLoader;

    protected boolean closeFs = true;

    @Override
    public void afterPropertiesSet() throws Exception {
//        Assert.isTrue(target != null || StringUtils.hasText(targetClassName) || (jar != null && jar.exists()),
//                "a target instance, class name or a Jar (with Main-Class) is required");
    }

    protected Configuration resolveConfiguration() throws Exception {
        Configuration cfg = ConfigurationUtils.createFrom(configuration, properties);
        if (jar != null) {
            String jarUrl = jar.getURL().toString();
            cfg.set(JOB_JAR_LOCATION_KEY, jarUrl);
        }
        buildGenericOptions(cfg);
        return cfg;
    }

    protected ClassLoader createCLForJar(Resource jar,
                                         ClassLoader parentCL, Configuration cfg) {
        return ExecutionUtils.createParentLastClassLoader(jar, parentCL, cfg);
    }

    protected Class<T> loadClass(String className, ClassLoader cl) {
        return (Class<T>) ClassUtils.resolveClassName(className, cl);
    }

    protected Class<T> resolveTargetClass(Configuration cfg) throws IOException {
        ClassLoader cl = beanClassLoader;
        if (target == null) {
            cl = createCLForJar(jar, cl, cfg);

            cfg.setClassLoader(cl);

            if (jar != null) {
                if (!StringUtils.hasText(targetClassName)) {
                    String mainClass = JarUtils.getMainClass(jar);
                    if (StringUtils.isEmpty(mainClass))
                        throw new RuntimeException("no Main-Class available");
                    targetClassName = mainClass;
                }
            } else {
                if (!StringUtils.hasText(targetClassName))
                    throw new RuntimeException("no target object available");
            }
            return loadClass(targetClassName, cl);
        }
        return (Class<T>) target.getClass();
    }

    protected T resolveTargetObject(Class<T> type) {
        return target != null ? target : BeanUtils.instantiateClass(type);
    }

    protected void preExecution(Configuration cfg) {
    }

    protected void postExecution(Configuration cfg) {
    }

    protected abstract Object invokeTargetObject(Configuration cfg,
                                                 T target, Class<T> targetClass, String[] args) throws Exception;

    private Integer invokeTarget(Configuration cfg, T target,
                                 Class<T> targetClass, String[] args) throws Exception {
        preExecution(cfg);
        try {
            Object result = invokeTargetObject(cfg, target, targetClass, args);
            if (result instanceof Integer) return (Integer) result;
            return Integer.valueOf(0);
        } finally {
            postExecution(cfg);
        }
    }

    public int runCode() throws Exception {
        final Configuration cfg = resolveConfiguration();
        final Class<T> type = resolveTargetClass(cfg);
        final T target = resolveTargetObject(type);

        Thread thread = Thread.currentThread();
        ClassLoader newCL = cfg.getClassLoader();
        ClassLoader oldCL = thread.getContextClassLoader();

        boolean isJarCL = newCL instanceof ParentLastURLClassLoader;
        if (isJarCL) {
            ExecutionUtils.preventHadoopLeaks(beanClassLoader);
        }
        try {
            thread.setContextClassLoader(newCL);

            if (StringUtils.hasText(user)) {
                // Proxy user to submit HBase command to server
                UserGroupInformation ugi = UserGroupInformation.createProxyUser(user,
                        UserGroupInformation.getLoginUser());
                return ugi.doAs(new PrivilegedExceptionAction<Integer>() {
                    @Override
                    public Integer run() throws Exception {
                        return invokeTarget(cfg, target, type, arguments);
                    }
                });
            } else {
                return invokeTarget(cfg, target, type, arguments);
            }
        } finally {
            thread.setContextClassLoader(oldCL);
            if (isJarCL) {
                if (closeFs) {
                    ExecutionUtils.shutdownFileSystem(cfg);
                }
                ExecutionUtils.patchLeakedClassLoader(newCL, oldCL);
            }
        }
    }

    public void setJar(Resource jar) {
        this.jar = jar;
    }

    public void setArguments(String... arguments) {
        this.arguments = arguments;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public void setBeanClassLoader(ClassLoader classLoader) {
        this.beanClassLoader = classLoader;
    }

    void setTargetObject(T target) {
        this.target = target;
    }

    void setTargetClassName(String targetClassName) {
        if (StringUtils.hasText(targetClassName))
            this.targetClassName = targetClassName;
    }

}
