package com.intel.soak.plugin.mapred.fi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

public class KillTaskThread extends Thread {

    private static final Log LOG = LogFactory.getLog(KillTaskThread.class);

    private volatile boolean killed = false;
    private RunningJob rJob;
    private JobClient jc;
    final private int thresholdMultiplier;
    private float threshold = 0.2f;
    private boolean onlyMapsProgress;
    private int numIterations;

    public KillTaskThread(JobClient jc, int thresholdMultiplier,
                          float threshold, boolean onlyMapsProgress, int numIterations) {
        this.jc = jc;
        this.thresholdMultiplier = thresholdMultiplier;
        this.threshold = threshold;
        this.onlyMapsProgress = onlyMapsProgress;
        this.numIterations = numIterations;
        setDaemon(true);
    }

    public void setRunningJob(RunningJob rJob) {
        this.rJob = rJob;
    }

    public void kill() {
        killed = true;
    }

    public void run() {
        killBasedOnProgress(true);
        if (!onlyMapsProgress) {
            killBasedOnProgress(false);
        }
    }

    private void killBasedOnProgress(boolean considerMaps) {
        boolean fail = false;
        if (considerMaps) {
            LOG.info("Will kill tasks based on Maps' progress");
        } else {
            LOG.info("Will kill tasks based on Reduces' progress");
        }
        LOG.info("Initial progress threshold: " + threshold +
                ". Threshold Multiplier: " + thresholdMultiplier +
                ". Number of iterations: " + numIterations);
        float thresholdVal = threshold;
        int numIterationsDone = 0;
        while (!killed) {
            try {
                float progress;
                if (jc.getJob(rJob.getID()).isComplete() ||
                        numIterationsDone == numIterations) {
                    break;
                }
                if (considerMaps) {
                    progress = jc.getJob(rJob.getID()).mapProgress();
                } else {
                    progress = jc.getJob(rJob.getID()).reduceProgress();
                }
                if (progress >= thresholdVal) {
                    numIterationsDone++;
                    if (numIterationsDone > 0 && numIterationsDone % 2 == 0) {
                        fail = true; //fail tasks instead of kill
                    }
                    ClusterStatus c = jc.getClusterStatus();

                    LOG.info(new Date() + " Killing a few tasks");

                    Collection<TaskAttemptID> runningTasks =
                            new ArrayList<TaskAttemptID>();
                    TaskReport mapReports[] = jc.getMapTaskReports(rJob.getID());
                    for (TaskReport mapReport : mapReports) {
                        if (mapReport.getCurrentStatus() == TIPStatus.RUNNING) {
                            runningTasks.addAll(mapReport.getRunningTaskAttempts());
                        }
                    }
                    if (runningTasks.size() > c.getTaskTrackers()/2) {
                        int count = 0;
                        for (TaskAttemptID t : runningTasks) {
                            LOG.info(new Date() + " Killed task : " + t);
                            rJob.killTask(t, fail);
                            if (count++ > runningTasks.size()/2) { //kill 50%
                                break;
                            }
                        }
                    }
                    runningTasks.clear();
                    TaskReport reduceReports[] = jc.getReduceTaskReports(rJob.getID());
                    for (TaskReport reduceReport : reduceReports) {
                        if (reduceReport.getCurrentStatus() == TIPStatus.RUNNING) {
                            runningTasks.addAll(reduceReport.getRunningTaskAttempts());
                        }
                    }
                    if (runningTasks.size() > c.getTaskTrackers()/2) {
                        int count = 0;
                        for (TaskAttemptID t : runningTasks) {
                            LOG.info(new Date() + " Killed task : " + t);
                            rJob.killTask(t, fail);
                            if (count++ > runningTasks.size()/2) { //kill 50%
                                break;
                            }
                        }
                    }
                    thresholdVal = thresholdVal * thresholdMultiplier;
                }
                Thread.sleep(5000);
            } catch (InterruptedException ie) {
                killed = true;
            } catch (Exception e) {
                LOG.fatal(StringUtils.stringifyException(e));
            }
        }
    }
}
