package com.intel.cedar.engine.impl;

import java.util.List;
import java.util.Properties;

import com.intel.cedar.engine.FeaturePropsInfo;
import com.intel.cedar.util.EntityUtil;
import com.intel.cedar.util.EntityWrapper;

public class FeaturePropsManager {
    private String featureName;
    private String defaultVersion;

    public FeaturePropsManager(String featureName, String version) {
        this.featureName = featureName;
        this.defaultVersion = version;
    }

    public Properties getProperties(String version) {
        EntityWrapper<FeaturePropsInfo> fdb = EntityUtil
                .getFeaturePropsEntityWrapper();
        Properties props = new Properties();
        try {
            FeaturePropsInfo i = new FeaturePropsInfo();
            i.setFeatureName(featureName);
            if (version == null || version.length() == 0)
                i.setFeatureVersion(defaultVersion);
            else
                i.setFeatureVersion(version);
            List<FeaturePropsInfo> r = fdb.query(i);
            for (FeaturePropsInfo f : r) {
                props.setProperty(f.getKey(), f.getValue());
            }
        } finally {
            fdb.rollback();
        }
        return props;
    }

    public void setProperties(Properties props, String version) {
        EntityWrapper<FeaturePropsInfo> fdb = EntityUtil
                .getFeaturePropsEntityWrapper();
        try {
            for (String key : props.stringPropertyNames()) {
                FeaturePropsInfo i = new FeaturePropsInfo();
                i.setFeatureName(featureName);
                if (version == null || version.length() == 0)
                    i.setFeatureVersion(defaultVersion);
                else
                    i.setFeatureVersion(version);
                i.setKey(key);
                List<FeaturePropsInfo> r = fdb.query(i);
                if (r.size() == 1) {
                    r.get(0).setValue(props.getProperty(key));
                } else {
                    i.setValue(props.getProperty(key));
                    fdb.add(i);
                }
            }
        } finally {
            fdb.commit();
        }
    }

    public String getProperty(String key, String version) {
        EntityWrapper<FeaturePropsInfo> fdb = EntityUtil
                .getFeaturePropsEntityWrapper();
        try {
            FeaturePropsInfo i = new FeaturePropsInfo();
            i.setFeatureName(featureName);
            if (version == null || version.length() == 0)
                i.setFeatureVersion(defaultVersion);
            else
                i.setFeatureVersion(version);
            i.setKey(key);
            List<FeaturePropsInfo> r = fdb.query(i);
            if (r.size() == 1) {
                return r.get(0).getValue();
            }
        } finally {
            fdb.rollback();
        }
        return null;
    }

    public void setProperty(String key, String value, String version) {
        EntityWrapper<FeaturePropsInfo> fdb = EntityUtil
                .getFeaturePropsEntityWrapper();
        try {
            FeaturePropsInfo i = new FeaturePropsInfo();
            i.setFeatureName(featureName);
            if (version == null || version.length() == 0)
                i.setFeatureVersion(defaultVersion);
            else
                i.setFeatureVersion(version);
            i.setKey(key);
            List<FeaturePropsInfo> r = fdb.query(i);
            if (r.size() == 1) {
                r.get(0).setValue(value);
            } else {
                i.setValue(value);
                fdb.add(i);
            }
        } finally {
            fdb.commit();
        }
    }
}
