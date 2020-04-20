package com.intel.bigdata.agent.ganglia.data;

public class GraphMetric {

    private String var;
    
    private String name;
    
    private String color;
    
    public GraphMetric(String var, String name, String color) {
        this.var = var;
        this.name = name;
        this.color = color;
    }
    
    public String getVar() {
        return var;
    }
    
    public String getName() {
        return name;
    }
    
    public String getColor() {
        return color;
    }
    
}
