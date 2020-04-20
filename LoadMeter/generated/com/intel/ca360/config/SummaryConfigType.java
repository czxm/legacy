
package com.intel.ca360.config;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SummaryConfigType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SummaryConfigType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ActiveUsers" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Name" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
@XmlType(name = "SummaryConfigType", propOrder = {
    "activeUsers",
    "name",
    "chartConfig"
})
public class SummaryConfigType {

    @XmlElement(name = "ActiveUsers", required = true)
    protected String activeUsers;
    @XmlElement(name = "Name", required = true)
    protected String name;
    @XmlElement(name = "ChartConfig")
    protected List<ChartConfigType> chartConfig;

    /**
     * Gets the value of the activeUsers property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getActiveUsers() {
        return activeUsers;
    }

    /**
     * Sets the value of the activeUsers property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setActiveUsers(String value) {
        this.activeUsers = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
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
