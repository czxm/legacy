package com.intel.soak.gauge;

import com.intel.soak.JobReport;
import com.intel.soak.model.GangliaMetricsConfigType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class GaugeReport {
    private static SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    long startTime;
    long duration;
    List<JobReport> jobReports;
    GangliaReport gangliaReport;
    GangliaMetricsConfigType config;

    public GaugeReport(){
        jobReports = new ArrayList<JobReport>();
    }

    public GangliaMetricsConfigType getConfig() {
        return config;
    }

    public void setConfig(GangliaMetricsConfigType config) {
        this.config = config;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public List<JobReport> getJobReports() {
        return jobReports;
    }

    public void setJobReports(List<JobReport> jobReports) {
        this.jobReports = jobReports;
    }

    public GangliaReport getGangliaReport() {
        return gangliaReport;
    }

    public void setGangliaReport(GangliaReport gangliaReport) {
        this.gangliaReport = gangliaReport;
    }
}
