package com.intel.soak.gauge.log.merger;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: joshua
 * Date: 10/22/13
 * Time: 9:58 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ILogMerger {

    enum MERGE_CATEGORY {
          ALL
    }

    void merge(String taskId);

    void merge(String taskId, MERGE_CATEGORY category);

}
