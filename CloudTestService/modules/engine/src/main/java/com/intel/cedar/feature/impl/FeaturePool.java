package com.intel.cedar.feature.impl;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.cedar.engine.model.DataModelException;
import com.intel.cedar.engine.model.feature.Feature;
import com.intel.cedar.feature.FeatureInfo;
import com.intel.cedar.feature.util.FeatureUtil;

public class FeaturePool {
    private static FeaturePool singleton;

    private HashMap<String, Feature> FEATUREPOOL = new HashMap<String, Feature>();

    private static Logger LOG = LoggerFactory.getLogger(FeaturePool.class);

    private FeaturePool() {
    }

    public synchronized static FeaturePool getInstance() {
        if (singleton == null) {
            singleton = new FeaturePool();
        }
        return singleton;
    }

    public Feature getFeature(String featureId) throws DataModelException {
        // TODO: temporaraly disable pooling, so that we can directly edit
        // feature.xml
        /*
         * Feature feature = FEATUREPOOL.get(featureId); if( feature!=null){
         * return feature; }
         */
        return loadFeature(featureId);
    }

    public void register(String featureID, Feature feature) {
        if (featureID == null) {
            return;
        }
        FEATUREPOOL.put(featureID, feature);
    }

    public void unRegister(String featureID) {
        if (featureID == null) {
            return;
        }
        FEATUREPOOL.remove(featureID);
    }

    protected Feature loadFeature(String featureId) {
        FeatureLoader loader = new FeatureLoader();

        Feature feature;
        try {
            FeatureInfo info = FeatureUtil.getFeatureInfoById(featureId);
            feature = loader.loadFeature(info);
            if (feature != null) {
                register(featureId, feature);
            }
        } catch (Exception e) {
            LOG.error("", e);
            return null;
        }

        return feature;
    }
}
