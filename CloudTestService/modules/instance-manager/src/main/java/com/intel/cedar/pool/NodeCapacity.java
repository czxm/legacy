package com.intel.cedar.pool;

public class NodeCapacity {
    private int cpu;
    private int mem;

    public NodeCapacity() {
    }

    public boolean isCapable(int cpu, int mem) {
        return this.cpu >= cpu && this.mem >= mem;
    }

    public void decreaseCapacity(int cpu, int mem) {
        this.cpu = this.cpu - cpu;
        this.mem = this.mem - mem;
    }

    public void increaseCapacity(int cpu, int mem) {
        this.cpu = this.cpu + cpu;
        this.mem = this.mem + mem;
    }

    public int getCpu() {
        return cpu;
    }

    public void setCpu(int cpu) {
        this.cpu = cpu;
    }

    public int getMem() {
        return mem;
    }

    public void setMem(int mem) {
        this.mem = mem;
    }
}
