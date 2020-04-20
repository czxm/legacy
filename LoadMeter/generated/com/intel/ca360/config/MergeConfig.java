
package com.intel.ca360.config;

import java.util.ArrayList;
import java.util.List;
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
 *         &lt;element name="LoadMeasureConfig" type="{http://www.intel.com/loadmeter/config}LoadMeasureConfigType"/>
 *         &lt;element name="RemoteMeasureConfig" type="{http://www.intel.com/loadmeter/config}RemoteMeasureConfigType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="SummaryConfig" type="{http://www.intel.com/loadmeter/config}SummaryConfigType"/>
 *         &lt;element name="Folder" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
    "loadMeasureConfig",
    "remoteMeasureConfig",
    "summaryConfig",
    "folder"
})
@XmlRootElement(name = "MergeConfig")
public class MergeConfig {

    @XmlElement(name = "LoadMeasureConfig", required = true)
    protected LoadMeasureConfigType loadMeasureConfig;
    @XmlElement(name = "RemoteMeasureConfig")
    protected List<RemoteMeasureConfigType> remoteMeasureConfig;
    @XmlElement(name = "SummaryConfig", required = true)
    protected SummaryConfigType summaryConfig;
    @XmlElement(name = "Folder", required = true)
    protected String folder;

    /**
     * Gets the value of the loadMeasureConfig property.
     * 
     * @return
     *     possible object is
     *     {@link LoadMeasureConfigType }
     *     
     */
    public LoadMeasureConfigType getLoadMeasureConfig() {
        return loadMeasureConfig;
    }

    /**
     * Sets the value of the loadMeasureConfig property.
     * 
     * @param value
     *     allowed object is
     *     {@link LoadMeasureConfigType }
     *     
     */
    public void setLoadMeasureConfig(LoadMeasureConfigType value) {
        this.loadMeasureConfig = value;
    }

    /**
     * Gets the value of the remoteMeasureConfig property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the remoteMeasureConfig property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRemoteMeasureConfig().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RemoteMeasureConfigType }
     * 
     * 
     */
    public List<RemoteMeasureConfigType> getRemoteMeasureConfig() {
        if (remoteMeasureConfig == null) {
            remoteMeasureConfig = new ArrayList<RemoteMeasureConfigType>();
        }
        return this.remoteMeasureConfig;
    }

    /**
     * Gets the value of the summaryConfig property.
     * 
     * @return
     *     possible object is
     *     {@link SummaryConfigType }
     *     
     */
    public SummaryConfigType getSummaryConfig() {
        return summaryConfig;
    }

    /**
     * Sets the value of the summaryConfig property.
     * 
     * @param value
     *     allowed object is
     *     {@link SummaryConfigType }
     *     
     */
    public void setSummaryConfig(SummaryConfigType value) {
        this.summaryConfig = value;
    }

    /**
     * Gets the value of the folder property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFolder() {
        return folder;
    }

    /**
     * Sets the value of the folder property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFolder(String value) {
        this.folder = value;
    }

}
