package com.intel.cedar.engine.xml.model;

public interface CharacterData extends Node {
    /**
     * Return the character value of the node.
     * 
     * @return the text of the node
     */
    public String getData();

    /**
     * Set the character value of the node.
     * 
     * @return
     */
    public void setData(String data);

    /**
     * Return the character length of the node.
     * 
     * @return the length of the text
     */
    public int getLength();
}
