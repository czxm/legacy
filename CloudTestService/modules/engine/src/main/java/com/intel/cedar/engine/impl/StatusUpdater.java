package com.intel.cedar.engine.impl;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.intel.cedar.engine.FeatureJobInfo;
import com.intel.cedar.engine.TaskRunnerInfo;
import com.intel.cedar.tasklet.TaskletInfo;

import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;

class StatusUpdater implements Runnable {
    private ConcurrentLinkedQueue<TaskletRuntimeInfo> tasklets;
    private FeatureJobInfo info;
    private boolean stopped;

    public StatusUpdater(ConcurrentLinkedQueue<TaskletRuntimeInfo> tasklets, FeatureJobInfo info) {
        this.tasklets = tasklets;
        this.info = info;
    }

    public void stop() {
        this.stopped = true;
    }

    public void run() {
        while (!stopped) {
            try{
                int totalCount = 0;
                int count = 0;
    
                info.clearTaskRunnerInfo();
                for (TaskletRuntimeInfo t : tasklets) {
                    totalCount += t.getItemCount();
                    TaskRunnerInfo runnerInfo = new TaskRunnerInfo();
                    runnerInfo.setTaskName(TaskletInfo.load(t.getTasklet().getID(),
                            t.getFeatureId()).getDesc());
                    runnerInfo.setStatus(t.getStatus());
                    int taskletTotalCount = t.getItemCount();
                    int accomplished = 0;
                    for (AgentRunner runner : t.getRunners()) {
                        accomplished += runner.getAccomplished();
                        count += runner.getAccomplished();
                    }
                    if (taskletTotalCount == 0) {
                        runnerInfo.setProgress(100);
                    } else {
                        runnerInfo.setProgress((int) (accomplished
                                / ((float) taskletTotalCount) * 100));
                    }
                    if(taskletTotalCount > 0 && taskletTotalCount > accomplished){
                        for (AgentRunner runner : t.getRunners()) {
                            runnerInfo.addAgentInfo(runner.getAgent().getAgentID(),
                                    runner.getHostName(), runner.getProgress(), runner
                                            .getStatus());
                        }
                    }
                    info.addTaskRunnerInfo(runnerInfo);
                }
                if (totalCount == 0) {
                    info.setPercent(0);
                } else {
                    info.setPercent((int) (count / ((float) totalCount) * 100));
                }
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                    break;
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}