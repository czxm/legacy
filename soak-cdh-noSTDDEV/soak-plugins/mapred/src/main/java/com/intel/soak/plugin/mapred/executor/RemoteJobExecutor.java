package com.intel.soak.plugin.mapred.executor;

import com.intel.soak.plugin.mapred.fi.KillTaskThread;
import com.intel.soak.plugin.mapred.fi.KillTrackerThread;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
//import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: joshua
 * Date: 11/15/13
 * Time: 3:27 AM
 * To change this template use File | Settings | File Templates.
 */
public class RemoteJobExecutor implements JobExecutor {

    private Tool jobClazz;
    private Configuration conf;
    private String[] params;
    private String hadoopArgs;
    private String fiType;

    private boolean needFaultInjection;

    public static transient Map<String, JobID> JOB_ID_MAP = new ConcurrentHashMap<String, JobID>();

    @Override
    public void init(String clazz, String[] params, String hadoopArgs) {
        try {
            Class toolClass = Thread.currentThread().getContextClassLoader().loadClass(clazz);
            this.jobClazz = (Tool) toolClass.newInstance();
            if (this.jobClazz == null)
                throw new RuntimeException("Init job class failed! Bean not found: " + clazz);
            this.params = params == null ? new String[0] : params;
            this.hadoopArgs = hadoopArgs;
            processConfWithHadoopArgs();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public Configuration getConf() {
        return conf;
    }

    public void setConf(Configuration conf) {
        this.conf = conf;
    }

    protected void processConfWithHadoopArgs() {
        if (StringUtils.isEmpty(hadoopArgs)) return;
        String[] parts = hadoopArgs.split(" ");
        for (String part : parts) {
            if (!StringUtils.isEmpty(part) && part.startsWith("-D")) {
                String[] hadoopArgParts = part.split("=");
                if (hadoopArgParts.length < 2
                        || StringUtils.isEmpty(hadoopArgParts[0])
                        || StringUtils.isEmpty(hadoopArgParts[1]))
                    continue;
                this.conf.set(hadoopArgParts[0].substring(2), hadoopArgParts[1]);
            } else {
                continue;
            }
        }
    }

    public static class RunJobThread extends Thread {
        private boolean isFinished;
        private boolean result;

        private Configuration conf;
        private Tool jobClazz;
        private String[] params;

        public RunJobThread(Configuration conf, Tool jobClazz, String[] params) {
            this.conf = conf;
            this.jobClazz = jobClazz;
            this.params = params;
        }

        public void run() {
            try {
                int rs = ToolRunner.run(conf, jobClazz, params);
                isFinished = true;
                result = rs == 0 ? true : false;
            } catch (Exception e) {
                e.printStackTrace();
                isFinished = true;
                result = false;
            }
        }

        public boolean isFinished() {
            return isFinished;
        }

        public boolean isResult() {
            return result;
        }
    }

    private RunJobThread runJob() {
        RunJobThread t = new RunJobThread(conf, jobClazz, params);
        t.setDaemon(true);
        t.start();
        return t;
    }

    private void killTasks(JobClient jc, JobID jobID) throws Exception {
        RunningJob rJob = jc.getJob(jobID);

        // Job has been completed. Skip running.
        if (rJob.isComplete()) return;  //TODO: LOG

        // Job not started running yet. Wait 1 second.
        while (rJob.getJobState() == JobStatus.PREP) {
            TimeUnit.SECONDS.sleep(1);
            rJob = jc.getJob(jobID);
        }

        KillTaskThread killTaskThread = new KillTaskThread(
                jc, 2, 0.2f, false, 2
        );
        killTaskThread.setRunningJob(rJob);
        killTaskThread.start();
        killTaskThread.join();
    }

    private void killTrackers(JobClient jc, JobID jobID) throws Exception {
        RunningJob rJob = jc.getJob(jobID);

        // Job has been completed. Skip running.
        if (rJob.isComplete()) return;  //TODO: LOG

        // Job not started running yet. Wait 1 second.
        while (rJob.getJobState() == JobStatus.PREP) {
            TimeUnit.SECONDS.sleep(1);
            rJob = jc.getJob(jobID);
        }
        KillTrackerThread killTrackerThread = new KillTrackerThread(
                jc, 0, 0.4f, false, 1
        );
        killTrackerThread.setRunningJob(rJob);
        killTrackerThread.start();
        killTrackerThread.join();
    }

    private JobID getJobId() {
        final long timeout = 10; // seconds
        long current = 0;
        while (JOB_ID_MAP.get(jobClazz.toString()) == null) {
            try {
                TimeUnit.SECONDS.sleep(1);
                if (++current >= timeout) break;
            } catch (InterruptedException e) {
            }
        }
        if (JOB_ID_MAP.get(jobClazz.toString()) == null) {
            throw new RuntimeException("Get job id timeout!");
        } else {
            return JOB_ID_MAP.get(jobClazz.toString());
        }
    }

    private void doFaultInjection() throws Exception {
        if (!needFaultInjection) return;
        JobID jobID = getJobId();
        JobClient jc = new JobClient(new JobConf(conf));
        if (!StringUtils.isEmpty(fiType)) {
            if ("task".equals(fiType))
                killTasks(jc, jobID);
            if ("tracker".equals(fiType))
                killTrackers(jc, jobID);
        }

    }

    @Override
    public boolean submit() {
        try {
            RunJobThread jobThread = runJob();
            doFaultInjection();
            jobThread.join();
            return jobThread.isFinished && jobThread.isResult();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void testFaultInjection(boolean isTest, String type) {
        this.needFaultInjection = isTest;
        this.fiType = type;
        if (needFaultInjection) {
            this.conf.setInt("mapred.map.max.attempts", 10);
            this.conf.setInt("mapred.reduce.max.attempts", 10);
        }
    }

    @Override
    public boolean clean() {
        FileSystem fs = null;
        try {
            fs = FileSystem.get(conf);
            fs.delete(new Path(params[2]), true);
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        } finally {
            /*
            if (fs != null) try {
                fs.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            */
        }
    }

}
