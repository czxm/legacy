package com.intel.cedar.tasklet.impl;

import java.util.ArrayList;
import java.util.List;

import com.intel.cedar.tasklet.ITaskItem;

public class PerfMonTaskItem extends GenericTaskItem implements ITaskItem {
    private static final long serialVersionUID = -4065996029444627912L;
    private String logFile = null;
    private int interval = 5;
    private List<Object> processes = new ArrayList<Object>();

    public String getValue() {
        StringBuilder sb = new StringBuilder();
        sb.append("Performance Monitor(Interval: ");
        sb.append(interval);
        for (Object o : processes) {
            sb.append(",");
            sb.append(o);
        }
        sb.append(")");
        return sb.toString();
    }

    public void setLogFile(String name) {
        this.logFile = name;
    }

    public String getLogFile() {
        return this.logFile;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getInterval() {
        return this.interval;
    }

    public void addProcess(int pid) {
        this.processes.add(new Integer(pid));
    }

    public void addProcess(String name) {
        addProcess(name, false);
    }

    public void addProcess(String name, boolean isJava) {
        if (isJava) {
            this.processes.add("java:" + name);
        } else {
            this.processes.add(name);
        }
    }

    public List<Object> getProcesses() {
        return this.processes;
    }
}
