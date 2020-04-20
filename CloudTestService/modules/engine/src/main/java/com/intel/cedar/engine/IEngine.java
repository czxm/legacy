package com.intel.cedar.engine;

import java.util.List;

import com.intel.cedar.core.CedarException;
import com.intel.cedar.engine.model.feature.Feature;
import com.intel.cedar.engine.model.feature.LaunchSet;

public interface IEngine {
    public Feature loadFeature(String feature) throws CedarException;

    public String submit(FeatureJobRequest request);

    public List<String> submit(String featureId, String launchset);

    public List<String> submit(String featureId, LaunchSet launchset);

    public void kill(String jobId);

    public void pause();

    public void unpause();

    public void shutdown();

    public List<FeatureDescriptor> listFeatures(List<String> features);

    public List<FeatureJobInfo> listFeatureJob(List<String> features);

    public FeatureJobInfo queryFeatureJob(String jobId);

    public void deployFeature(String path) throws CedarException;

    public void removeFeature(String feature) throws CedarException;

    public void updateFeature(Feature model) throws CedarException;
}