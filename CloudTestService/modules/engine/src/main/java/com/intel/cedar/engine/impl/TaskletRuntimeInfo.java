package com.intel.cedar.engine.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.intel.cedar.core.entities.VolumeInfo;
import com.intel.cedar.engine.EngineFactory;
import com.intel.cedar.engine.model.feature.flow.TaskletFlow;
import com.intel.cedar.tasklet.ITaskItem;
import com.intel.cedar.tasklet.ITaskRunner;

public class TaskletRuntimeInfo {
    private TaskletFlow tasklet;
    private String featureId;
    private String status;
    private ITaskRunner taskRunner;
    private List<AgentRunner> runners;
    private List<ITaskItem> items;
    private HashMap<ITaskItem, VolumeInfo> itemVolumeMap;
    private HashMap<ITaskItem, Object> itemMachineMap;
    private HashMap<ITaskItem, String> itemStorageMap;
    private ConcurrentLinkedQueue<ITaskItem> itemQueue;

    public TaskletRuntimeInfo(TaskletFlow f) {
        runners = new ArrayList<AgentRunner>();
        items = new ArrayList<ITaskItem>();
        itemVolumeMap = new HashMap<ITaskItem, VolumeInfo>();
        itemMachineMap = new HashMap<ITaskItem, Object>();
        itemStorageMap = new HashMap<ITaskItem, String>();
        itemQueue = new ConcurrentLinkedQueue<ITaskItem>();
        tasklet = f;
    }

    public void setFeatureId(String id) {
        this.featureId = id;
    }

    public String getFeatureId() {
        return featureId;
    }

    public TaskletFlow getTasklet() {
        return tasklet;
    }

    public List<AgentRunner> getRunners() {
        return runners;
    }

    public void addRunner(AgentRunner runner) {
        runners.add(runner);
    }

    public ConcurrentLinkedQueue<ITaskItem> getWorkingQueue() {
        return itemQueue;
    }

    public List<ITaskItem> getItems() {
        return items;
    }

    public int getItemCount() {
        return items.size();
    }

    public ITaskRunner getTaskRunner() {
        return taskRunner;
    }
    
    public ITaskRunner cloneTaskRunner() {
        ITaskRunner cloned = null;
        try{
            Object o = Class.forName(
                taskRunner.getClass().getCanonicalName(),
                true,
                EngineFactory.getInstance().getEngine()
                        .loadFeature(featureId)
                        .getFeatureClassLoader()).newInstance();
            if (o instanceof ITaskRunner) {
                cloned = (ITaskRunner) o;
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return cloned;
    }

    public void setTaskRunner(ITaskRunner runner) {
        this.taskRunner = runner;
    }

    public void addItem(ITaskItem item) {
        itemQueue.offer(item);
        items.add(item);
    }

    public void setItemStorage(ITaskItem item, String s) {
        itemStorageMap.put(item, s);
    }

    public String getItemStorage(ITaskItem item) {
        return itemStorageMap.get(item);
    }

    public void setItemVolume(ITaskItem item, VolumeInfo v) {
        itemVolumeMap.put(item, v);
    }

    public VolumeInfo getItemVolume(ITaskItem item) {
        return itemVolumeMap.get(item);
    }

    public void setItemMachine(ITaskItem item, Object m) {
        itemMachineMap.put(item, m);
    }

    public Object getItemMachine(ITaskItem item) {
        return itemMachineMap.get(item);
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }
}
