package com.intel.cedar.features.demo;

import java.util.List;

import com.intel.cedar.feature.AbstractFeature;
import com.intel.cedar.feature.Environment;
import com.intel.cedar.feature.TaskSummaryItem;

public class DemoFeature extends AbstractFeature{
	@Override
	protected List<TaskSummaryItem> getSummaryItems(Environment env){
		return null;
	}
}
