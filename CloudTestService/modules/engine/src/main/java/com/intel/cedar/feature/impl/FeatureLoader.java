package com.intel.cedar.feature.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.cedar.engine.model.feature.Feature;
import com.intel.cedar.engine.model.feature.flow.FeatureFlow;
import com.intel.cedar.engine.model.loader.FeatureDescLoader;
import com.intel.cedar.feature.FeatureInfo;
import com.intel.cedar.feature.util.FeatureUtil;
import com.intel.cedar.service.client.feature.model.ui.FeatureModel;

public class FeatureLoader {

    public FeatureLoader() {
    }

    public Feature loadFeature(FeatureInfo info) throws Exception {
        if (info == null) {
            return null;
        }
        return getFeature(info);
    }

    public FeatureFlow loadFeatureFlow(String featureId) throws Exception {
        FeatureInfo featureInfo = FeatureInfo.load(featureId);
        String descriptor = featureInfo.getDescriptor();
        if (descriptor == null)
            return null;

        File file = FeatureUtil.getFile(featureInfo.getId(), descriptor);
        InputStream is = new FileInputStream(file);

        FeatureDescLoader loader = new FeatureDescLoader();
        return loader.loadFeatureFlow(is);
    }

    protected Feature getFeature(FeatureInfo featureInfo) throws Exception {
        String jarName = featureInfo.getJar();
        File jar = FeatureUtil.getFile(featureInfo.getId(), jarName);
        if (false == jar.exists()) {
            throw new Exception("Can't find the jar: " + jarName + ".");
        }

        // load feature
        String jarPath = jar.getAbsolutePath();
        String[] jarPaths = addDependJars(jarPath, featureInfo);
        FeatureJar featureJar = new FeatureJar(jarPaths);

        Feature feature = getFeatureDescriptor(featureInfo, featureJar);
        feature.setName(featureInfo.getName());
        feature.setVersion(featureInfo.getVersion());

        // set the class loader
        feature.setFeatureClassLoader(featureJar.getClassLoader());

        // set the feature model
        FeatureModel featureModel = feature.getFeatureModel();
        featureModel.setFeatureID(featureInfo.getId());

        return feature;
    }

    protected Feature getFeatureDescriptor(FeatureInfo featureInfo,
            FeatureJar featureJar) throws Exception {
        String descriptor = featureInfo.getDescriptor();
        if (descriptor == null)
            return null;

        File file = FeatureUtil.getFile(featureInfo.getId(), descriptor);
        InputStream is = new FileInputStream(file);

        FeatureDescLoader loader = new FeatureDescLoader();
        loader.setFeatureJar(featureJar);
        loader.setClassLoader(featureJar.getClassLoader());

        return loader.load(is).getFeature();
    }

    protected String[] addDependJars(String inputJar, FeatureInfo featureInfo) {
        List<String> libs = new ArrayList<String>();
        libs.add(".");
        libs.add(inputJar);
        libs.addAll(featureInfo.getDependsLibs());
        String[] jars = new String[libs.size()];
        int i = 0;
        for (String jar : libs) {
            File file = FeatureUtil.getFile(featureInfo.getId(), jar);
            jars[i++] = file.toString();
        }
        return jars;
    }
}
