/**
 * 
 */
package com.intel.cedar.feature.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

/**
 * @author yzhou51
 * 
 */
public class JarUtility {
    public static class JavaClass {
        String name;
        Class<?> classObj;

        public JavaClass(String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }

        public void setClassObj(Class<?> classObj) {
            this.classObj = classObj;
        }
    }

    public static ClassLoader loadJar(String[] jarStrings) {
        URL[] jars = new URL[jarStrings.length];
        for (int index = 0; index < jarStrings.length; ++index) {
            try {
                jars[index] = new File(jarStrings[index].trim()).toURL();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        return loadJar(jars);
    }

    public static ClassLoader loadJar(URL[] jars) {
        try {
            return URLClassLoader.newInstance(jars, JarUtility.class
                    .getClassLoader());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Class<?> loadClass(String cls, ClassLoader loader) {
        try {
            return (Class<?>) loader.loadClass(cls);
        } catch (Throwable e) {
            return null;
        }
    }

    public static List getJavaClassList(InputStream inputStream) {
        ArrayList list = new ArrayList();
        try {
            JarInputStream jarFile = new JarInputStream(inputStream);
            JarEntry e = jarFile.getNextJarEntry();
            while (null != e) {
                if (e.getName().endsWith(".class")
                        && !e.getName().contains("$")) {
                    list.add(e.getName().replaceAll("/", "\\.").replace(
                            ".class", ""));
                }
                e = jarFile.getNextJarEntry();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return list;
    }

    public static Manifest getMainfest(String path) {
        JarInputStream jarFile = null;
        try {
            jarFile = new JarInputStream(new FileInputStream(path.trim()));
            return jarFile.getManifest();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (jarFile != null)
                    jarFile.close();
            } catch (Exception e) {
            }
        }
    }

    public static List getJavaClassList(String jarList) {
        ArrayList list = new ArrayList();
        try {
            for (String jar : jarList.split("\\|")) {
                JarInputStream jarFile = new JarInputStream(
                        new FileInputStream(jar.trim()));
                JarEntry e = jarFile.getNextJarEntry();
                while (null != e) {
                    if (e.getName().endsWith(".class")
                            && !e.getName().contains("$")) {
                        list.add(e.getName().replaceAll("/", "\\.").replace(
                                ".class", ""));
                    }
                    e = jarFile.getNextJarEntry();
                }
            }
        } catch (Exception e) {
        }
        return list;
    }

    public static List getPublicJavaMethods(Class<?> clz) {
        ArrayList list = new ArrayList();
        try {
            Method[] methods = clz.getDeclaredMethods();
            for (Method m : methods) {
                if (Modifier.isPublic(m.getModifiers()))
                    list.add(m);
            }
        } catch (Throwable e) {
        }
        return list;
    }

    public static List<String> getAllInterfaces(Class<?> clz) {
        List<String> interfaceList = new ArrayList<String>();
        Class[] theInterfaces = clz.getInterfaces();
        for (int i = 0; i < theInterfaces.length; i++) {
            String interfaceName = theInterfaces[i].getName();
            interfaceList.add(interfaceName);
            interfaceList.addAll(getAllInterfaces(theInterfaces[i]));
        }
        return interfaceList;
    }

    public static boolean isInterfaceMatch(Class<?> clz, String interfaceName) {
        if (clz == null) {
            return false;
        }

        List<String> interfaceList = JarUtility.getAllInterfaces(clz);

        return interfaceList.contains(interfaceName);
    }
}
