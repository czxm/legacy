package com.intel.bigdata.agent.ganglia.data;

public enum GraphSize {
    
    xlarge(650, 300);

    private final int width;
    private final int height;
    
    private GraphSize(int width, int height) {
        this.width = width;
        this.height = height;
    }
    
    public int getHeight() {
        return height;
    }
    
    public int getWidth() {
        return width;
    }
    
}
