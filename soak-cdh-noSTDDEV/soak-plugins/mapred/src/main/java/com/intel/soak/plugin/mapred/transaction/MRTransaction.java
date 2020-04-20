package com.intel.soak.plugin.mapred.transaction;

import com.intel.soak.plugin.annotation.PLUGIN_TYPE;
import com.intel.soak.plugin.annotation.Plugin;
import com.intel.soak.plugin.mapred.executor.JobExecutor;
import com.intel.soak.plugin.mapred.executor.RemoteJobExecutor;
import com.intel.soak.transaction.AbstractTransaction;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;

import java.util.Random;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: joshua
 * Date: 11/16/13
 * Time: 2:53 AM
 * To change this template use File | Settings | File Templates.
 */
@Plugin(desc = "MR Transaction", type = PLUGIN_TYPE.TRANSACTION)
public class MRTransaction extends AbstractTransaction {

    private JobExecutor executor;
    private Configuration conf = new Configuration();

    @Override
    public boolean beforeExecute() {
        try {
            String execObj = getParamValue("execObj");
            String params = getParamValue("jobParams");
            String hadoopArgs = getParamValue("hadoopArgs");
            Boolean enableFaultInjection = Boolean.valueOf(getParamValue("isReliability"));
            String fiType = getParamValue("faultInjectionType");
            if (executor instanceof RemoteJobExecutor)
                ((RemoteJobExecutor) executor).setConf(conf);
            executor.testFaultInjection(enableFaultInjection, fiType);
            executor.init(execObj, resolveParams(params), hadoopArgs);
            executor.clean();
            return true;
        } catch (Throwable e) {
            logger.error("Execute transaction failed: " + e.toString());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean execute() {
        try {
            return executor.submit();
        } catch (Throwable e) {
            logger.error("Execute transaction failed: " + e.toString());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean afterExecute() {
        return executor.clean();
    }

    private String[] resolveParams(String paramStr) {
        if (StringUtils.isEmpty(paramStr)) return null;
        String[] params = paramStr.split(" ");
        String[] args = new String[params.length];
        Random random = new Random();
        for (int i = 0; i < params.length; i++) {
            String value = params[i];
            if (value.contains("%UUID%")) {
                args[i] = value.replaceAll("\\%UUID\\%", UUID.randomUUID().toString());
            } else if (value.contains("%randomInt%")) {
                args[i] = value.replaceAll("\\%randomInt\\%}", String.valueOf(random.nextInt()));
            } else {
                args[i] = value;
            }
        }
        return args;
    }

    @Override
    public void kill() {
    }

    // DI
    public void setExecutor(JobExecutor executor) {
        this.executor = executor;
    }

    public Configuration getConf() {
        return conf;
    }

    public void setConf(Configuration conf) {
        this.conf = conf;
    }

}
