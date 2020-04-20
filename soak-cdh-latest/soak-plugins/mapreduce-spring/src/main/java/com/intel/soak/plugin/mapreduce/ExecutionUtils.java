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
package com.intel.soak.plugin.mapreduce;

import com.intel.soak.utils.JarUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalDirAllocator;
import org.apache.hadoop.mapred.Counters;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.Permission;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import static com.intel.soak.plugin.mapreduce.MapredPluginConstants.*;


/**
 * Code execution utilities.
 *
 * @author Costin Leau
 * @author Jarred Li
 */
/**
 * Modified for soak
 */
public enum ExecutionUtils {

    INSTANCE;

    private static final Log log = LogFactory.getLog(ExecutionUtils.class);

    private static boolean isLegacyJar(Resource jar) throws IOException {
        JarInputStream jis = new JarInputStream(jar.getInputStream());
        JarEntry entry = null;
        try {
            while ((entry = jis.getNextJarEntry()) != null) {
                String name = entry.getName();
                if (name.startsWith("lib/"))
                    return true;
            }
        } finally {
            IOUtils.closeQuietly(jis);
        }
        return false;
    }

    private static File detectBaseDir(Configuration cfg) throws IOException {
        File tmpDir = null;
        if (cfg != null) {
            tmpDir = new File(cfg.get(JOB_TMP_DIR_KEY));
            tmpDir.mkdirs();
            if (!tmpDir.isDirectory()) tmpDir = null;
        }
        final File workDir = File.createTempFile("hadoop-unjar", "", tmpDir);
        workDir.delete();
        workDir.mkdirs();

        return workDir;
    }

    private static URL[] expandedJarClassPath(Resource jar, Configuration cfg)
            throws IOException {

        File baseDir = detectBaseDir(cfg);
        JarUtils.unjar(jar, baseDir);

        List<URL> cp = new ArrayList<URL>();
        cp.add(new File(baseDir + File.separator).toURI().toURL());

        File[] libs = new File(baseDir, "lib").listFiles();
        if (libs != null) {
            for (File lib : libs) {
                cp.add(lib.toURI().toURL());
            }
        }
        return cp.toArray(new URL[cp.size()]);
    }

    public static ClassLoader createParentLastClassLoader(Resource jar,
            ClassLoader parentCL, Configuration cfg) {

        ClassLoader cl = null;
        if (parentCL == null) {
            parentCL = ClassUtils.getDefaultClassLoader();
            cl = parentCL;
        }

        if (jar != null) {
            try {
                URL[] urls = null;
                if (isLegacyJar(jar)) {  // Jar has its dependency jars in lib dir of its binary.
                    urls = expandedJarClassPath(jar, cfg);
                } else {
                    urls = new URL[] { jar.getURL() };
                }
                cl = new ParentLastURLClassLoader(urls, parentCL);
            } catch (IOException e) {
                throw new IllegalStateException("Cannot open jar file", e);
            }
        }

        return cl;
    }

    public static String[] resolveParams(String paramStr) {
        if (!StringUtils.hasText(paramStr)) return null;
        String[] params = paramStr.split(" ");
        String[] args = new String[params.length];
        Random random = new Random();
        for (int i = 0; i < params.length; i++) {
            String value = params[i];
            if (value.contains("%UUID%")) {
                args[i] = value.replaceAll("\\%UUID\\%",
                        UUID.randomUUID().toString());
            } else if (value.contains("%randomInt%")) {
                args[i] = value.replaceAll("\\%randomInt\\%}",
                        String.valueOf(random.nextInt()));
            } else {
                args[i] = value;
            }
        }
        return args;
    }

    public static Properties loadPropsFromClassPath(ClassLoader cl, String propFile) {
        if (StringUtils.hasText(propFile)) {
            InputStream is = null;
            try {
                Properties props = new Properties();
                is = cl.getResourceAsStream(propFile);
                props.load(is);
                return props;
            } catch (Throwable e) {
                e.printStackTrace(); //TODO: LOG
                return null;
            } finally {
                IOUtils.closeQuietly(is);
            }
        } else {
            return null;
        }
    }

    public static void preventHadoopLeaks(ClassLoader hadoopCL) {

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            // set the sysCL as the TCCL
            Thread.currentThread().setContextClassLoader(ClassLoader.getSystemClassLoader());

            // fix org.apache.hadoop.mapred.Counters#MAX_COUNTER_LIMIT
            // calling constructor since class loading is lazy
            new Counters();
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    public static void shutdownFileSystem(Configuration cfg) {
        FileSystem fs;
        try {
            fs = FileSystem.get(cfg);
            if (fs != null) {
                fs.close();
            }
        } catch (Exception ex) {
        }
        try {
            fs = FileSystem.getLocal(cfg);
            if (fs != null) {
                fs.close();
            }
        } catch (Exception ex) {
        }
    }

    static Thread[] threads() {
        // Could have used the code below but it tends to be somewhat ineffective and slow
        // Set<Thread> threadSet = Thread.getAllStackTraces().keySet();

        // Get the current thread group
        ThreadGroup tg = Thread.currentThread().getThreadGroup();
        // Find the root thread group
        while (tg.getParent() != null) {
            tg = tg.getParent();
        }

        int threadCountGuess = tg.activeCount() + 50;
        Thread[] threads = new Thread[threadCountGuess];
        int threadCountActual = tg.enumerate(threads);
        // Make sure we don't miss any threads
        while (threadCountActual == threadCountGuess) {
            threadCountGuess *= 2;
            threads = new Thread[threadCountGuess];
            // Note tg.enumerate(Thread[]) silently ignores any threads that
            // can't fit into the array
            threadCountActual = tg.enumerate(threads);
        }

        return threads;
    }

    private static final Set<String> JVM_THREAD_NAMES = new HashSet<String>();

    static {
        JVM_THREAD_NAMES.add("system");
        JVM_THREAD_NAMES.add("RMI Runtime");
    }

    private static void replaceTccl(ClassLoader leakedClassLoader, ClassLoader replacementClassLoader) {
        for (Thread thread : threads()) {
            if (thread != null) {
                ClassLoader cl = thread.getContextClassLoader();
                // do identity check to prevent expensive (and potentially dangerous) equals()
                if (leakedClassLoader == cl) {
                    log.warn("Trying to patch leaked cl [" + leakedClassLoader + "] in thread [" + thread + "]");
                    ThreadGroup tg = thread.getThreadGroup();
                    // it's a JVM thread so use the System ClassLoader always
                    boolean debug = log.isDebugEnabled();
                    if (tg != null && JVM_THREAD_NAMES.contains(tg.getName())) {
                        thread.setContextClassLoader(ClassLoader.getSystemClassLoader());
                        if (debug) {
                            log.debug("Replaced leaked cl in thread [" + thread + "] with system classloader");
                        }
                    }
                    else {
                        thread.setContextClassLoader(replacementClassLoader);
                        if (debug) {
                            log.debug("Replaced leaked cl in thread [" + thread + "] with " + replacementClassLoader);
                        }
                    }
                }
            }
        }
    }

    private static Field CLASS_CACHE;
    private static Method UTILS_CONSTRUCTOR_CACHE;

    static {
        CLASS_CACHE = ReflectionUtils.findField(Configuration.class, "CACHE_CLASS");
        if (CLASS_CACHE != null) {
            ReflectionUtils.makeAccessible(CLASS_CACHE);
        }

        UTILS_CONSTRUCTOR_CACHE = ReflectionUtils.findMethod(org.apache.hadoop.util.ReflectionUtils.class, "clearCache");
        ReflectionUtils.makeAccessible(UTILS_CONSTRUCTOR_CACHE);
    }

    private static void fixHadoopReflectionUtilsLeak(ClassLoader leakedClassLoader) {
        // replace Configuration#CLASS_CACHE in Hadoop 2.0 which prevents CL from being recycled
        // this is a best-effort really as the leak can occur again - see HADOOP-8632

        // only available on Hadoop-2.0/CDH4
        if (CLASS_CACHE == null) {
            return;
        }

        Map<?, ?> cache = (Map<?, ?>) ReflectionUtils.getField(CLASS_CACHE, null);
        cache.remove(leakedClassLoader);
    }

    private static void fixHadoopReflectionUtilsLeak() {
        // org.apache.hadoop.util.ReflectionUtils.clearCache();
        ReflectionUtils.invokeMethod(UTILS_CONSTRUCTOR_CACHE, null);
    }

    private static void cleanHadoopLocalDirAllocator() {
        Field field = ReflectionUtils.findField(LocalDirAllocator.class, "contexts");
        ReflectionUtils.makeAccessible(field);
        Map contexts = (Map) ReflectionUtils.getField(field, null);
        if (contexts != null) {
            contexts.clear();
        }
    }

    public static void patchLeakedClassLoader(ClassLoader leakedClassLoader, ClassLoader replacementClassLoader) {
        replaceTccl(leakedClassLoader, replacementClassLoader);
        fixHadoopReflectionUtilsLeak(leakedClassLoader);
        fixHadoopReflectionUtilsLeak();
        cleanHadoopLocalDirAllocator();
    }

}
