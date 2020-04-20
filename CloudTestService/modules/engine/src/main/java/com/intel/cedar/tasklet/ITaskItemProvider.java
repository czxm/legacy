package com.intel.cedar.tasklet;

import java.util.List;

import com.intel.cedar.feature.Environment;

public interface ITaskItemProvider {
    public List<ITaskItem> getTaskItems(Environment env);
}
