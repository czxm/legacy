
package com.intel.ca360.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DelayType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DelayType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element name="FixDelay" type="{http://www.intel.com/loadmeter/config}FixDelayType"/>
 *         &lt;element name="VariableDelay" type="{http://www.intel.com/loadmeter/config}VariableDelayType"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DelayType", propOrder = {
    "fixDelay",
    "variableDelay"
})
public class DelayType {

    @XmlElement(name = "FixDelay")
    protected FixDelayType fixDelay;
    @XmlElement(name = "VariableDelay")
    protected VariableDelayType variableDelay;

    /**
     * Gets the value of the fixDelay property.
     * 
     * @return
     *     possible object is
     *     {@link FixDelayType }
     *     
     */
    public FixDelayType getFixDelay() {
        return fixDelay;
    }

    /**
     * Sets the value of the fixDelay property.
     * 
     * @param value
     *     allowed object is
     *     {@link FixDelayType }
     *     
     */
    public void setFixDelay(FixDelayType value) {
        this.fixDelay = value;
    }

    /**
     * Gets the value of the variableDelay property.
     * 
     * @return
     *     possible object is
     *     {@link VariableDelayType }
     *     
     */
    public VariableDelayType getVariableDelay() {
        return variableDelay;
    }

    /**
     * Sets the value of the variableDelay property.
     * 
     * @param value
     *     allowed object is
     *     {@link VariableDelayType }
     *     
     */
    public void setVariableDelay(VariableDelayType value) {
        this.variableDelay = value;
    }

}
