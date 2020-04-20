package com.intel.soak.transaction;


import com.intel.soak.gauge.GaugeMetrics;

import java.util.List;

public class TransactionSummary{
    String name;
    List<GaugeMetrics> summary;

    public TransactionSummary(String name, List<GaugeMetrics> summary){
        this.name = name;
        this.summary = summary;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHTMLHeaders(){
        if(summary.size() > 0)
            return summary.get(0).getHTMLHeaders();
        else
            return "N/A";
    }

    public List<GaugeMetrics> getMetricsList() {
        return summary;
    }

}
