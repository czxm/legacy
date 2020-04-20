
package com.intel.ca360.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for TaskConfigType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TaskConfigType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Iterations" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="Delay" type="{http://www.intel.com/loadmeter/config}DelayType"/>
 *         &lt;element name="TaskDriver" type="{http://www.intel.com/loadmeter/config}TaskDriverType"/>
 *       &lt;/sequence>
 *       &lt;attribute name="duration" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="syncStartup" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="negRate" type="{http://www.w3.org/2001/XMLSchema}float" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TaskConfigType", propOrder = {
    "iterations",
    "delay",
    "taskDriver"
})
public class TaskConfigType {

    @XmlElement(name = "Iterations")
    protected int iterations;
    @XmlElement(name = "Delay", required = true)
    protected DelayType delay;
    @XmlElement(name = "TaskDriver", required = true)
    protected TaskDriverType taskDriver;
    @XmlAttribute
    protected Integer duration;
    @XmlAttribute
    protected Boolean syncStartup;
    @XmlAttribute
    protected Float negRate;

    /**
     * Gets the value of the iterations property.
     * 
     */
    public int getIterations() {
        return iterations;
    }

    /**
     * Sets the value of the iterations property.
     * 
     */
    public void setIterations(int value) {
        this.iterations = value;
    }

    /**
     * Gets the value of the delay property.
     * 
     * @return
     *     possible object is
     *     {@link DelayType }
     *     
     */
    public DelayType getDelay() {
        return delay;
    }

    /**
     * Sets the value of the delay property.
     * 
     * @param value
     *     allowed object is
     *     {@link DelayType }
     *     
     */
    public void setDelay(DelayType value) {
        this.delay = value;
    }

    /**
     * Gets the value of the taskDriver property.
     * 
     * @return
     *     possible object is
     *     {@link TaskDriverType }
     *     
     */
    public TaskDriverType getTaskDriver() {
        return taskDriver;
    }

    /**
     * Sets the value of the taskDriver property.
     * 
     * @param value
     *     allowed object is
     *     {@link TaskDriverType }
     *     
     */
    public void setTaskDriver(TaskDriverType value) {
        this.taskDriver = value;
    }

    /**
     * Gets the value of the duration property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getDuration() {
        return duration;
    }

    /**
     * Sets the value of the duration property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setDuration(Integer value) {
        this.duration = value;
    }

    /**
     * Gets the value of the syncStartup property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isSyncStartup() {
        return syncStartup;
    }

    /**
     * Sets the value of the syncStartup property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSyncStartup(Boolean value) {
        this.syncStartup = value;
    }

    /**
     * Gets the value of the negRate property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getNegRate() {
        return negRate;
    }

    /**
     * Sets the value of the negRate property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setNegRate(Float value) {
        this.negRate = value;
    }

}
