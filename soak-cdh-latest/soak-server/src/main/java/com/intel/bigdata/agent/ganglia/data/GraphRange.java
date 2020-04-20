package com.intel.bigdata.agent.ganglia.data;

public enum GraphRange {
    
    day(86400, "last day");
    
    // # of seconds
    private final int duration;
    
    private final String description;
    
    /**
     * 
     * @param duration in seconds
     */
    private GraphRange(int duration, String description) {
        this.duration = duration;
        this.description = description;
    }
    
    public int getDuration() {
        return duration;
    }
    
    public String getDescription() {
        return description;
    }
    
}
