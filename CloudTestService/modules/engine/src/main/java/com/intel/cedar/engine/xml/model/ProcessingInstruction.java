package com.intel.cedar.engine.xml.model;

public interface ProcessingInstruction extends Node {

    /**
     * Get the content of this processing instruction.
     * 
     * @return the content of the processing instruction
     */
    public String getData();

    /**
     * Get the target of this processing instruction.
     * 
     * @return the target of the processing instruction
     */
    public String getTarget();

    /**
     * Set the content of processing instruction
     * 
     * @return
     */
    public void setData(String data);

}
