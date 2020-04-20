package com.intel.soak.plugin.mapred.executor;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: joshua
 * Date: 11/15/13
 * Time: 3:14 AM
 * To change this template use File | Settings | File Templates.
 */
public interface JobExecutor {

    /**
     * Initial a Job
     *
     * @param execObj       Execution object, could be a mr jar or a mr class
     * @param params        Job parameters
     */
    void init(String execObj, String[] params, String hadoopArgs);

    /**
     * Submit a Job
     *
     * @return  result
     */
    boolean submit();

    /**
     * If enabling fault injection, test jobs should be protected against
     * the case of jobs failing even when multiple attempts fail, set some
     * high values for the max attempts.
     * <p>
     * Note that the following configuration items will be replaced by the
     * ones with the same name user specified in the transaction element
     * <code>hadoopArgs</code>.
     *
     * <p>mapred.map.max.attempts=10
     * <p>mapred.reduce.max.attempts=10
     */
    void testFaultInjection(boolean isTest, String type);

    boolean clean();

}
