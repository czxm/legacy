package com.intel.cedar.engine.xml.model;

public interface Attribute extends Node {

    /**
     * Get the owner element which contains the attribute
     */
    public Element getOwnerElement();

    /**
     * Get the value of the attribute is returned as a string.
     * 
     * @return the value of the attribute
     */
    public String getValue();

    /**
     * Set the value of the attribute node.
     * 
     * @return
     */
    public void setValue(String value);

}
