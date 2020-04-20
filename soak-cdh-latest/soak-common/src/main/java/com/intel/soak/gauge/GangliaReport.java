package com.intel.soak.gauge;

import java.util.ArrayList;
import java.util.List;

public class GangliaReport {

    List<GaugeMetrics> gangliaMetrics;
    List<GaugeChart> charts;

    public GangliaReport(){
        gangliaMetrics = new ArrayList<GaugeMetrics>();
        charts = new ArrayList<GaugeChart>();
    }

    public List<GaugeMetrics> getGangliaMetrics() {
        return gangliaMetrics;
    }

    public void setGangliaMetrics(List<GaugeMetrics> gangliaMetrics) {
        this.gangliaMetrics = gangliaMetrics;
    }

    public List<GaugeChart> getCharts() {
        return charts;
    }

    public void setCharts(List<GaugeChart> charts) {
        this.charts = charts;
    }
}
