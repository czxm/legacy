package com.intel.soak.logger;

/** 
 * The abstract Cluster Logger structure <p>
 * @author xzhan27
 *
 */

public abstract class AbstractAgentLogger implements SoakLogger{
    protected String source;
    protected String component;
    protected String type;
    
    public AbstractAgentLogger(String type){
        this.type = type;
    }
    
    public String getSource(){
        return source;
    }
    
    public String getComponent(){
        return component;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setComponent(String component) {
        this.component = component;
    }
}