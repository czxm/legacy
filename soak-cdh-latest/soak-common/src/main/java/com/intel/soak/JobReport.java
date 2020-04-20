package com.intel.soak;

import com.intel.soak.gauge.GaugeChart;
import com.intel.soak.gauge.GaugeMetrics;
import com.intel.soak.model.MergeConfig;
import com.intel.soak.transaction.TransactionSummary;
import com.intel.soak.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class JobReport {
    String name;
    String description;
    long startTime;
    long duration;
    List<GaugeMetrics> detailMetrics;
    List<GaugeChart> charts;
    List<TransactionSummary> summaries;
    MergeConfig config;

    public JobReport(){
        detailMetrics = new ArrayList<GaugeMetrics>();
        summaries = new ArrayList<TransactionSummary>();
        charts = new ArrayList<GaugeChart>();
    }

    public MergeConfig getConfig() {
        return config;
    }

    public void setConfig(MergeConfig config) {
        this.config = config;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<GaugeMetrics> getDetailMetrics() {
        return detailMetrics;
    }

    public void setDetailMetrics(List<GaugeMetrics> detailMetrics) {
        this.detailMetrics = detailMetrics;
    }

    public List<GaugeChart> getCharts() {
        return charts;
    }

    public void setCharts(List<GaugeChart> charts) {
        this.charts = charts;
    }

    public List<TransactionSummary> getSummaries() {
        return summaries;
    }

    public void setSummaries(List<TransactionSummary> summaries) {
        this.summaries = summaries;
    }

    public String getHTMLHeaders(){
        return detailMetrics.get(0).getHTMLHeaders();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getStartDate(){
        return DateTimeUtils.dateToStdTimeString(new Date(startTime));
    }

    public String getDurationText(){
        return DateTimeUtils.millisToDuration(duration);
    }

    public List<GaugeChart> getMajorCharts(){
        List<GaugeChart> charts = new ArrayList<GaugeChart>();
        for(GaugeChart c : this.getCharts()){
            if(c.getName().startsWith("response")){
                charts.add(c);
            }
        }
        return charts;
    }
}
