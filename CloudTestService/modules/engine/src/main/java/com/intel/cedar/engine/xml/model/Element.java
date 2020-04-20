package com.intel.cedar.engine.xml.model;

import com.intel.cedar.engine.xml.XMLException;

public interface Element extends Node {

    /**
     * Remove an existing attribute node and return the removed attribute
     * 
     */
    Attribute removeAttributeNode(Attribute oldAttr) throws XMLException;

    /**
     * Removes an attribute by local name and namespace URI
     * 
     */
    Attribute removeAttribute(String namespaceURI, String localName)
            throws XMLException;

    /**
     * Adds a new attribute.
     * 
     */
    Attribute setAttributeNode(Attribute newAttr) throws XMLException;

    /**
     * Adds a new attribute.
     * 
     */
    Attribute setAttribute(String namespaceURI, String qualifiedName,
            String value) throws XMLException;
}
