
package com.intel.ca360.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for VariableDelayType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="VariableDelayType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="MinDelay" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="MaxDelay" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VariableDelayType", propOrder = {
    "minDelay",
    "maxDelay"
})
public class VariableDelayType {

    @XmlElement(name = "MinDelay")
    protected int minDelay;
    @XmlElement(name = "MaxDelay")
    protected int maxDelay;

    /**
     * Gets the value of the minDelay property.
     * 
     */
    public int getMinDelay() {
        return minDelay;
    }

    /**
     * Sets the value of the minDelay property.
     * 
     */
    public void setMinDelay(int value) {
        this.minDelay = value;
    }

    /**
     * Gets the value of the maxDelay property.
     * 
     */
    public int getMaxDelay() {
        return maxDelay;
    }

    /**
     * Sets the value of the maxDelay property.
     * 
     */
    public void setMaxDelay(int value) {
        this.maxDelay = value;
    }

}
