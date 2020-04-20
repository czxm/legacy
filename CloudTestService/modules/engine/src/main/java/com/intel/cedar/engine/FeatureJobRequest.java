package com.intel.cedar.engine;

import java.util.List;

import com.intel.cedar.service.client.feature.model.Variable;

public class FeatureJobRequest {
    public Long userId; // who submitted this job ( could be 0 when scheduled )
    public String featureId; // the feature of this job
    public List<Variable> variables; // the variables collected
    public String description; // the comments which will be in the report
    public boolean reproducable; // this job is supposed to be reproducable
    public List<String> receivers; // all email address who will receive the
    public List<String> failure_receivers; // all email address who will receive the failed report
}
