package com.intel.cedar.feature.impl;

import java.io.InputStream;

import com.intel.cedar.feature.util.JarUtility;

public class FeatureJar {
    private String[] jarPath;
    private ClassLoader cLoader;

    public static void main(String args[]) {
        FeatureJar loader = new FeatureJar(
                new String[] { "examples\\features\\cpp.conformance.jar" });
        loader.getClassLoader();
        loader.getResourceStream("feature.xml");
    }

    public FeatureJar(String[] jarPath) {
        this.jarPath = jarPath;
    }

    public ClassLoader getClassLoader() {
        if (cLoader == null) {
            computeClassLoader();
        }
        return cLoader;
    }

    public InputStream getResourceStream(String name) {
        ClassLoader loader = getClassLoader();
        if (loader == null) {
            return null;
        }

        return loader.getResourceAsStream(name);
    }

    protected void computeClassLoader() {
        if (jarPath == null) {
            return;
        }

        cLoader = JarUtility.loadJar(jarPath);
    }
}
