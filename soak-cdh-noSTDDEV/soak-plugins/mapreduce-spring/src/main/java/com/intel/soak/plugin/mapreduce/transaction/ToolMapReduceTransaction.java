package com.intel.soak.plugin.mapreduce.transaction;

import com.intel.soak.plugin.annotation.PLUGIN_TYPE;
import com.intel.soak.plugin.annotation.Plugin;
import com.intel.soak.plugin.mapreduce.ExecutionUtils;
import com.intel.soak.plugin.mapreduce.executor.ToolExecutor;
import org.apache.hadoop.util.Tool;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: joshua
 * Date: 12/3/13
 * Time: 9:46 PM
 * To change this template use File | Settings | File Templates.
 */
@Plugin(desc = "Tool MapReduce Transaction", type = PLUGIN_TYPE.TRANSACTION)
public class ToolMapReduceTransaction extends AbstractMapReduceTransaction<ToolExecutor> {

    private void validate() {
        String targetRef = getParamValue("tool-ref");
        Object target = null;

        //TODO: need soak framework to insert job config.
//        if (StringUtils.hasText(targetRef))
//            target = SpringBeanFactoryManager.getPluginAppCxt().getBean(targetRef);

        String targetClassName = getParamValue("tool-class");

        Resource jar = null;
        String jarPath = getParamValue("jar");
        if (StringUtils.hasText(jarPath))
            jar = new FileSystemResource(jarPath);

        Assert.isTrue(target != null && target instanceof Tool
                || StringUtils.hasText(targetClassName)
                || (jar != null && jar.exists()),
                "a target Tool instance, class name or a Jar (with Main-Class) is required");
    }

    @Override
    public boolean beforeExecute() {
        try {
            super.beforeExecute();

            validate();

            String jarPath = getParamValue("jar");
            if (StringUtils.hasText(jarPath)) {
                Resource jar = new FileSystemResource(new File(jarPath));
                executor.setJar(jar);
            }

            //TODO: need soak framework to insert job config.

//            String targetRef = getParamValue("tool-ref");
//            if (StringUtils.hasText(targetRef)) {
//                Object obj = SpringBeanFactoryManager.getPluginAppCxt().getBean(targetRef);
//                executor.setTool((Tool) obj);
//            }

            String targetClassName = getParamValue("tool-class");
            if (StringUtils.hasText(targetClassName)) {
                executor.setToolClass(targetClassName);
            }

            String argStr = getParamValue("arguments");
            executor.setArguments(ExecutionUtils.resolveParams(argStr));

            String hadoopArgStr = getParamValue("hadoopArgs");
            ClassLoader cl = this.getClass().getClassLoader();
            executor.setProperties(ExecutionUtils.loadPropsFromClassPath(cl, hadoopArgStr));

            return true;
        } catch (Throwable e) {
            logger.error("Startup Tool MapReduce transaction failed: " + e.toString());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void kill() {
    }

}
