/**
 * 
 */
package com.intel.cedar.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import com.intel.cedar.engine.impl.FeatureRunner;
import com.intel.cedar.storage.IFolder;
import com.intel.cedar.util.CedarConfiguration;

public class FeatureJobInfo implements Comparable<FeatureJobInfo> {

    private String id; // auto generated job id
    private Long userId; // user
    private Long submitTime; // submission time
    private Long endTime; // finish time
    private String featureId; // feature
    private FeatureStatus status; // succeeded, killed or failed
    private String desc; // job desc
    private IFolder storage; // storage location
    private List<String> receivers; // any other emails who will receive the report                                   
    private List<String> failure_receivers; // any other emails who will receive the failed report
    private List<TaskRunnerInfo> tasks;
    private int percent;
    private FeatureRunner theRunner;
    private boolean reproducable;
    private boolean sendReport;

    public boolean isSendReport() {
        return sendReport;
    }

    public void setSendReport(boolean sendReport) {
        this.sendReport = sendReport;
    }

    private Future<?> future;

    /**
	 * 
	 */

    public FeatureJobInfo() {
        tasks = new ArrayList<TaskRunnerInfo>();
    }

    public void clearTaskRunnerInfo() {
        synchronized (this) {
            tasks.clear();
        }
    }

    public void addTaskRunnerInfo(TaskRunnerInfo info) {
        synchronized (this) {
            tasks.add(info);
        }
    }

    public List<TaskRunnerInfo> getTaskRunnerInfo() {
        synchronized (this) {
            List<TaskRunnerInfo> res = new ArrayList<TaskRunnerInfo>();
            for (TaskRunnerInfo t : tasks) {
                res.add(t);
            }

            return res;
        }
    }

    @Override
    public int compareTo(FeatureJobInfo o) {
        return submitTime.compareTo(o.submitTime);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(Long submitTime) {
        this.submitTime = submitTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public String getFeatureId() {
        return featureId;
    }

    public void setFeatureId(String featureId) {
        this.featureId = featureId;
    }

    public FeatureStatus getStatus() {
        return status;
    }

    public void setStatus(FeatureStatus status) {
        this.status = status;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    public Future<?> getFuture() {
        return future;
    }

    public void setFuture(Future<?> future) {
        this.future = future;
    }

    public boolean isReproducable() {
        return reproducable;
    }

    public void setReproducable(boolean reproducable) {
        this.reproducable = reproducable;
    }

    public IFolder getStorage() {
        return this.storage;
    }

    public void setStorage(IFolder folder) {
        this.storage = folder;
    }
    
    public String getLocation(){
        return CedarConfiguration.getStorageServiceURL() + "?cedarURL="
                + storage.getURI().toString();
    }

    public FeatureRunner getRunner() {
        return theRunner;
    }

    public void setRunner(FeatureRunner theRunner) {
        this.theRunner = theRunner;
    }

    public List<String> getReceivers() {
        return receivers;
    }

    public void setReceivers(List<String> receivers) {
        this.receivers = receivers;
    }
    
    public List<String> getFailureReceivers() {
        return failure_receivers;
    }

    public void setFailureReceivers(List<String> receivers) {
        this.failure_receivers = receivers;
    }
}
