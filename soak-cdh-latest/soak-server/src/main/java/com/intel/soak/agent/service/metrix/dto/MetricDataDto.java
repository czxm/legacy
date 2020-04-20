package com.intel.soak.agent.service.metrix.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: joshua
 * Date: 10/24/13
 * Time: 10:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class MetricDataDto {

    private String metric;

    private Date start;
    private Date end;

    private List<Date> time = new ArrayList<Date>();
    private List<Double> data = new ArrayList<Double>();


    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public List<Date> getTime() {
        return time;
    }

    public List<Double> getData() {
        return data;
    }

    public void add(Date time, Double data) {
        this.time.add(time);
        this.data.add(data);
    }

    public long getSize() {
        return this.time.size();
    }

}
