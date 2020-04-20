package com.intel.soak.plugin.mapreduce.transaction;

import com.intel.soak.plugin.mapreduce.ExecutionUtils;
import com.intel.soak.plugin.mapreduce.JobGenericOptions;
import com.intel.soak.transaction.AbstractTransaction;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.net.MalformedURLException;

/**
 * Created with IntelliJ IDEA.
 * User: joshua
 * Date: 12/3/13
 * Time: 8:48 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractMapReduceTransaction<T extends JobGenericOptions> extends AbstractTransaction {

    protected T executor;

    protected Resource[] strArray2ResourceArray(String[] files) {
        if (ArrayUtils.isEmpty(files)) return null;
        String resourcePath = null;
        Resource[] res = new Resource[files.length];
        for (int i = 0; i < files.length; i++) {
            resourcePath = files[i];
            try {
                res[i] = new UrlResource(resourcePath);
            } catch (MalformedURLException e) {
                logger.error(String.format("Generate resource[%s] failed: %s", resourcePath, e.toString()));
                e.printStackTrace();
                continue;
            }
        }
        return res;
    }

    @Override
    public boolean beforeExecute() {
        String[] libs = ExecutionUtils.resolveParams(getParamValue("libs"));
        String[] files = ExecutionUtils.resolveParams(getParamValue("files"));
        String[] archives = ExecutionUtils.resolveParams(getParamValue("archives"));
        executor.setLibs(strArray2ResourceArray(libs));
        executor.setArchives(strArray2ResourceArray(archives));
        executor.setFiles(strArray2ResourceArray(files));
        executor.setUser(getParamValue("user"));
        return true;
    }

    @Override
    public boolean execute() {
        try {
            return (executor.runCode() == 0) ? true : false;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public T getExecutor() {
        return executor;
    }

    public void setExecutor(T executor) {
        this.executor = executor;
    }

}
