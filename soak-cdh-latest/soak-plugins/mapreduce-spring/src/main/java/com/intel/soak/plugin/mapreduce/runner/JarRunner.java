package com.intel.soak.plugin.mapreduce.runner;

import com.intel.soak.plugin.mapreduce.executor.JarExecutor;

import java.util.concurrent.Callable;

/**
 * Created with IntelliJ IDEA.
 * User: joshua
 * Date: 12/3/13
 * Time: 8:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class JarRunner extends JarExecutor implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        return runCode();
    }

}
