package com.intel.soak.gauge;

import java.util.ArrayList;
import java.util.List;

import com.intel.soak.MetricsData;
import com.intel.soak.MetricsData.Aggregator;


/** 
 * This class represents the actual DTO for metrics
 * <p>
 * It wraps the internal MetricsData together with a cluster timestamp
 * @author xzhan27
 * @see com.intel.soak.MetricsData
 */

public class GaugeMetrics extends GaugeEntity {
    private static final long serialVersionUID = 3453855124099951757L;
    
    protected String source;
    protected Long timestamp;
    protected List<MetricsData> metrics;
    
    public Long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public void setMetricsValue(String category, Float d){
        MetricsData m = findMetricsByCategory(category);
        if(m == null){
            m = new MetricsData(category);
            m.setAggregators(new Aggregator[]{Aggregator.AVG});
            getMetrics().add(m);
        }
        m.setMetricValue(Aggregator.AVG, d);
    }
    
    public List<MetricsData> getMetrics() {
        if(metrics == null){
            metrics = new ArrayList<MetricsData>();
        }
        return metrics;
    }
    
    public void setMetrics(List<MetricsData> metrics) {
        this.metrics = metrics;
    }
    
    public MetricsData findMetricsByCategory(String name){
        for(MetricsData d : getMetrics()){
            if(d.getCategory().equals(name))
                return d;
        }
        return null;
    }

    public List<String> getMetricsCategories(){
        List<String> names = new ArrayList<String>();
        for(MetricsData data : getMetrics()){
            names.add(data.getCategory());
        }
        return names;
    }
    
    public List<String> getMetricsNames() {
        List<String> names = new ArrayList<String>();
        for(MetricsData data : getMetrics()){
            names.addAll(data.getMetricsNames());
        }
        return names;
    }
    
    public List<Float> getMetricsValues() {
        List<Float> values = new ArrayList<Float>();
        for(MetricsData data : getMetrics()){
            values.addAll(data.getMetricsValues());
        }
        return values;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if(timestamp != null)
            sb.append(String.format("%d,", timestamp));
        for(MetricsData t : getMetrics()){
            for(Float f : t.getMetricsValues()){            
                sb.append(f == null ? "," : String.format("%.2f,", f));
            }
        }
        return sb.toString();
    }
    
    public String toHTMLString() {
        StringBuilder sb = new StringBuilder();
        if(timestamp != null)
            sb.append(String.format("<TD>%d</TD>", timestamp));
        for(MetricsData t : getMetrics()){
            for(Float f : t.getMetricsValues()){            
                sb.append(f == null ? "<TD></TD>" : String.format("<TD>%.2f</TD>", f));
            }
        }
        return sb.toString();
    }

    public String getHTMLHeaders(){
        StringBuilder sb = new StringBuilder();
        sb.append("<TH>Timestamp</TH>");
        for(MetricsData t : getMetrics()){
            for(String s : t.getMetricsNames()){
                sb.append(String.format("<TH>%s</TH>", s));
            }
        }
        return sb.toString();
    }
    
    public void merge(GaugeMetrics otherMetrics){
        for(int i = 0; i < metrics.size(); i++){
            MetricsData d1 = metrics.get(i);
            MetricsData d2 = otherMetrics.metrics.get(i);
            d1.mergeMetrics(d2);
        }
    }
    
    public void commit(){
        for(MetricsData d : metrics){
            d.commit();
        }
    }
    
    public void average(int number){
        for(MetricsData d : getMetrics()){
            d.average(number);
        }
    }
    
    public GaugeMetrics clone(){
        GaugeMetrics m = new GaugeMetrics();
        m.source = this.source;
        m.timestamp = this.timestamp;
        m.metrics = new ArrayList<MetricsData>();
        for(MetricsData md : getMetrics()){
            m.metrics.add(md.clone());
        }
        return m;
    }
    
    public static GaugeMetrics fromMetricsNames(String[] names){
        if(names == null)
            return null;
        GaugeMetrics metrics = new GaugeMetrics();
        for(int i = 1; i < names.length; i++){
            String name = names[i];
            int underscore = name.lastIndexOf("_");
            String aggregator = name.substring(underscore + 1);
            Aggregator aggr = null;
            try{
                aggr = Aggregator.valueOf(aggregator);
            }
            catch(Exception e){                
            }
            if(aggr != null){
                String category = name.substring(0, underscore);
                MetricsData md = metrics.findMetricsByCategory(category);
                if(md == null){
                    md = new MetricsData(category);
                    metrics.getMetrics().add(md);
                }
                md.addAggregators(new Aggregator[]{aggr});
            }
        }
        return metrics;
    }
}