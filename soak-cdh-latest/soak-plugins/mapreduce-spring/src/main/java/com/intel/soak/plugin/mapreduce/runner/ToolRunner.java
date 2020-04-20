package com.intel.soak.plugin.mapreduce.runner;

import com.intel.soak.plugin.mapreduce.executor.ToolExecutor;

import java.util.concurrent.Callable;

/**
 * Created with IntelliJ IDEA.
 * User: joshua
 * Date: 12/4/13
 * Time: 1:28 AM
 * To change this template use File | Settings | File Templates.
 */
public class ToolRunner extends ToolExecutor implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        return runCode();
    }

}
