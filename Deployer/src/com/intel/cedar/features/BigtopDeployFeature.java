package com.intel.cedar.features;

import java.util.List;

import com.intel.cedar.feature.AbstractFeature;
import com.intel.cedar.feature.Environment;
import com.intel.cedar.feature.TaskSummaryItem;


public class BigtopDeployFeature extends AbstractFeature {

    @Override
    protected List<TaskSummaryItem> getSummaryItems(Environment arg0) {
        return null;
    }

    @Override
    public void onInit(Environment env) throws Exception{
        
    }
    
    @Override
    public void onFinalize(Environment env) throws Exception{
        super.onFinalize(env);
    }
}
