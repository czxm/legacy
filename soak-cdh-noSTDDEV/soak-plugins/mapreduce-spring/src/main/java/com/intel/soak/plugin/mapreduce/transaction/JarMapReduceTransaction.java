package com.intel.soak.plugin.mapreduce.transaction;

import com.intel.soak.plugin.annotation.PLUGIN_TYPE;
import com.intel.soak.plugin.annotation.Plugin;
import com.intel.soak.plugin.mapreduce.ExecutionUtils;
import com.intel.soak.plugin.mapreduce.executor.JarExecutor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: joshua
 * Date: 12/2/13
 * Time: 11:53 PM
 * To change this template use File | Settings | File Templates.
 */
@Plugin(desc = "Jar MapReduce Transaction", type = PLUGIN_TYPE.TRANSACTION)
public class JarMapReduceTransaction extends AbstractMapReduceTransaction<JarExecutor> {

    private void validate() {
        String libPath = getParamValue("jar");
        Assert.hasText(libPath, "MapReduce jar is required!");
        Resource jar = new FileSystemResource(new File(libPath));
        if (!jar.exists()) {
            throw new IllegalStateException(
                    "The MR job jar does not exist: " + libPath);
        }
    }

    @Override
    public boolean beforeExecute() {
        try {
            super.beforeExecute();

            validate();

            String libPath = getParamValue("jar");
            Resource jar = new FileSystemResource(new File(libPath));
            executor.setJar(jar);

            String argStr = getParamValue("arguments");
            executor.setArguments(ExecutionUtils.resolveParams(argStr));

            String hadoopArgStr = getParamValue("hadoopArgs");
            ClassLoader cl = this.getClass().getClassLoader();
            executor.setProperties(ExecutionUtils.loadPropsFromClassPath(cl, hadoopArgStr));

            String main = getParamValue("Main-Class");
            executor.setMainClass(main);

            return true;
        } catch (Throwable e) {
            logger.error("Startup Jar MapReduce transaction failed: " + e.toString());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void kill() {

    }

}
