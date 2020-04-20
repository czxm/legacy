
package com.intel.ca360.config;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for BatchConfigType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BatchConfigType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Interval" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="BatchUsers" type="{http://www.w3.org/2001/XMLSchema}int" maxOccurs="unbounded"/>
 *         &lt;element name="Rampup" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BatchConfigType", propOrder = {
    "interval",
    "batchUsers",
    "rampup"
})
public class BatchConfigType {

    @XmlElement(name = "Interval")
    protected int interval;
    @XmlElement(name = "BatchUsers", type = Integer.class)
    protected List<Integer> batchUsers;
    @XmlElement(name = "Rampup")
    protected int rampup;

    /**
     * Gets the value of the interval property.
     * 
     */
    public int getInterval() {
        return interval;
    }

    /**
     * Sets the value of the interval property.
     * 
     */
    public void setInterval(int value) {
        this.interval = value;
    }

    /**
     * Gets the value of the batchUsers property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the batchUsers property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBatchUsers().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Integer }
     * 
     * 
     */
    public List<Integer> getBatchUsers() {
        if (batchUsers == null) {
            batchUsers = new ArrayList<Integer>();
        }
        return this.batchUsers;
    }

    /**
     * Gets the value of the rampup property.
     * 
     */
    public int getRampup() {
        return rampup;
    }

    /**
     * Sets the value of the rampup property.
     * 
     */
    public void setRampup(int value) {
        this.rampup = value;
    }

}
