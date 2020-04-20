
package com.intel.ca360.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="VirtualUserConfig" type="{http://www.intel.com/loadmeter/config}VirtualUserConfigType"/>
 *         &lt;element name="BatchConfig" type="{http://www.intel.com/loadmeter/config}BatchConfigType" minOccurs="0"/>
 *         &lt;element name="TaskConfig" type="{http://www.intel.com/loadmeter/config}TaskConfigType"/>
 *         &lt;element name="Description" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="MonitorConfig" type="{http://www.intel.com/loadmeter/config}MonitorConfigType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "virtualUserConfig",
    "batchConfig",
    "taskConfig",
    "description",
    "monitorConfig"
})
@XmlRootElement(name = "LoadConfig")
public class LoadConfig {

    @XmlElement(name = "VirtualUserConfig", required = true)
    protected VirtualUserConfigType virtualUserConfig;
    @XmlElement(name = "BatchConfig")
    protected BatchConfigType batchConfig;
    @XmlElement(name = "TaskConfig", required = true)
    protected TaskConfigType taskConfig;
    @XmlElement(name = "Description", required = true)
    protected String description;
    @XmlElement(name = "MonitorConfig")
    protected MonitorConfigType monitorConfig;

    /**
     * Gets the value of the virtualUserConfig property.
     * 
     * @return
     *     possible object is
     *     {@link VirtualUserConfigType }
     *     
     */
    public VirtualUserConfigType getVirtualUserConfig() {
        return virtualUserConfig;
    }

    /**
     * Sets the value of the virtualUserConfig property.
     * 
     * @param value
     *     allowed object is
     *     {@link VirtualUserConfigType }
     *     
     */
    public void setVirtualUserConfig(VirtualUserConfigType value) {
        this.virtualUserConfig = value;
    }

    /**
     * Gets the value of the batchConfig property.
     * 
     * @return
     *     possible object is
     *     {@link BatchConfigType }
     *     
     */
    public BatchConfigType getBatchConfig() {
        return batchConfig;
    }

    /**
     * Sets the value of the batchConfig property.
     * 
     * @param value
     *     allowed object is
     *     {@link BatchConfigType }
     *     
     */
    public void setBatchConfig(BatchConfigType value) {
        this.batchConfig = value;
    }

    /**
     * Gets the value of the taskConfig property.
     * 
     * @return
     *     possible object is
     *     {@link TaskConfigType }
     *     
     */
    public TaskConfigType getTaskConfig() {
        return taskConfig;
    }

    /**
     * Sets the value of the taskConfig property.
     * 
     * @param value
     *     allowed object is
     *     {@link TaskConfigType }
     *     
     */
    public void setTaskConfig(TaskConfigType value) {
        this.taskConfig = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the monitorConfig property.
     * 
     * @return
     *     possible object is
     *     {@link MonitorConfigType }
     *     
     */
    public MonitorConfigType getMonitorConfig() {
        return monitorConfig;
    }

    /**
     * Sets the value of the monitorConfig property.
     * 
     * @param value
     *     allowed object is
     *     {@link MonitorConfigType }
     *     
     */
    public void setMonitorConfig(MonitorConfigType value) {
        this.monitorConfig = value;
    }

}
