package com.intel.soak.plugin.mapred.executor;

import com.intel.bigdata.common.util.Command;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: joshua
 * Date: 11/16/13
 * Time: 2:55 AM
 * To change this template use File | Settings | File Templates.
 */
public class LocalJobExecutor implements JobExecutor {

    private String mapredHome;
    private String mrCommand;

    private String jar;
    private String[] params;
    private String hadoopArgs = "";

    @Override
    public void init(String jar, String[] params, String hadoopArgs) {
        try {
            this.jar = new File(jar).getCanonicalPath();
            this.params = params;
            this.hadoopArgs = hadoopArgs;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getMRArgs() {
        if (params == null) return null;
        StringBuilder sb = new StringBuilder();
        for (String param : params) {
           sb.append(" ").append(param);
        }
        return sb.toString();
    }

    @Override
    public boolean submit() {
        try {
            String command = String.format("%s%s%s %s %s %s",
                    mapredHome, File.separator, mrCommand,
                    jar, this.hadoopArgs, getMRArgs()
            );
            return Command.execute(command) == 0 ? true : false;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void testFaultInjection(boolean isTest, String type) {
        if (isTest)
            this.hadoopArgs += "-Dmapred.map.max.attempts=10 -Dmapred.reduce.max.attempts=10 ";
    }

    @Override
    public boolean clean() {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setMapredHome(String mapredHome) {
        this.mapredHome = mapredHome;
    }

    public void setMrCommand(String mrCommand) {
        this.mrCommand = mrCommand;
    }

}
