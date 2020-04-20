package com.intel.cedar.engine;

public enum FeatureStatus {
    Submitted, Started, Cancelled, Failed, Finished, Evicted;

    public boolean isStarted() {
        return this.equals(Started);
    }
    
    public boolean isStopped() {
        return this.equals(Cancelled) || this.equals(Failed) || this.equals(Finished);
    }
}
