
package com.intel.ca360.config;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for LoadMeasureConfigType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="LoadMeasureConfigType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="FilesConfig" type="{http://www.intel.com/loadmeter/config}FilesConfigType"/>
 *         &lt;element name="MergeResult" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ChartConfig" type="{http://www.intel.com/loadmeter/config}ChartConfigType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LoadMeasureConfigType", propOrder = {
    "filesConfig",
    "mergeResult",
    "chartConfig"
})
public class LoadMeasureConfigType {

    @XmlElement(name = "FilesConfig", required = true)
    protected FilesConfigType filesConfig;
    @XmlElement(name = "MergeResult", required = true)
    protected String mergeResult;
    @XmlElement(name = "ChartConfig")
    protected List<ChartConfigType> chartConfig;

    /**
     * Gets the value of the filesConfig property.
     * 
     * @return
     *     possible object is
     *     {@link FilesConfigType }
     *     
     */
    public FilesConfigType getFilesConfig() {
        return filesConfig;
    }

    /**
     * Sets the value of the filesConfig property.
     * 
     * @param value
     *     allowed object is
     *     {@link FilesConfigType }
     *     
     */
    public void setFilesConfig(FilesConfigType value) {
        this.filesConfig = value;
    }

    /**
     * Gets the value of the mergeResult property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMergeResult() {
        return mergeResult;
    }

    /**
     * Sets the value of the mergeResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMergeResult(String value) {
        this.mergeResult = value;
    }

    /**
     * Gets the value of the chartConfig property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the chartConfig property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getChartConfig().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ChartConfigType }
     * 
     * 
     */
    public List<ChartConfigType> getChartConfig() {
        if (chartConfig == null) {
            chartConfig = new ArrayList<ChartConfigType>();
        }
        return this.chartConfig;
    }

}
