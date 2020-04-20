package com.intel.bigdata.agent.ganglia.data;

public enum GraphPreset {

    mapred_report("Mapreduce Slots", "Slots", new GraphMetric[] {
            new GraphMetric("maps_running", "mapred.tasktracker.maps_running", "FF0000"),
            new GraphMetric("reduces_running", "mapred.tasktracker.reduces_running", "00FF00"),
            new GraphMetric("mapTaskSlots", "mapred.tasktracker.mapTaskSlots", "2030F4"),
            new GraphMetric("reduceTaskSlots", "mapred.tasktracker.reduceTaskSlots", "9900CC") }),
            
    hbaserequestcount_report("HBase Regionserver Request count", "request count", new GraphMetric[] {
            new GraphMetric("readRequestsCount", "hbase.regionserver.readRequestsCount", "00FF00"),
            new GraphMetric("writeRequestsCount", "hbase.regionserver.writeRequestsCount", "FF0000")
    });

    private String title;
    private String verticalLabel;
    private GraphMetric[] metrics;

    private GraphPreset(String title, String verticalLable, GraphMetric[] metrics) {
        this.title = title;
        this.verticalLabel = verticalLable;
        this.metrics = metrics;
    }
    
    public String getTitle() {
        return title;
    }
    
    public GraphMetric[] getMetrics() {
        return metrics;
    }
    
    public String getVerticalLabel() {
        return verticalLabel;
    }

}
