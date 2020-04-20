
package com.intel.ca360.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ChartConfigType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ChartConfigType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="XAxisName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="XSeries" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="YAxisName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="YSeries" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="YSeriesLabel" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SecondYAxisName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SecondYSeries" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SecondYSeriesLabel" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Title" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *       &lt;attribute name="width" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="height" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="datasetSize" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="titleFontSize" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="labelFontSize" type="{http://www.w3.org/2001/XMLSchema}int" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ChartConfigType", propOrder = {
    "xAxisName",
    "xSeries",
    "yAxisName",
    "ySeries",
    "ySeriesLabel",
    "secondYAxisName",
    "secondYSeries",
    "secondYSeriesLabel",
    "title"
})
public class ChartConfigType {

    @XmlElement(name = "XAxisName", required = true)
    protected String xAxisName;
    @XmlElement(name = "XSeries", required = true)
    protected String xSeries;
    @XmlElement(name = "YAxisName", required = true)
    protected String yAxisName;
    @XmlElement(name = "YSeries", required = true)
    protected String ySeries;
    @XmlElement(name = "YSeriesLabel")
    protected String ySeriesLabel;
    @XmlElement(name = "SecondYAxisName")
    protected String secondYAxisName;
    @XmlElement(name = "SecondYSeries")
    protected String secondYSeries;
    @XmlElement(name = "SecondYSeriesLabel")
    protected String secondYSeriesLabel;
    @XmlElement(name = "Title", required = true)
    protected String title;
    @XmlAttribute
    protected Integer width;
    @XmlAttribute
    protected Integer height;
    @XmlAttribute(required = true)
    protected String name;
    @XmlAttribute
    protected Integer datasetSize;
    @XmlAttribute
    protected Integer titleFontSize;
    @XmlAttribute
    protected Integer labelFontSize;

    /**
     * Gets the value of the xAxisName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getXAxisName() {
        return xAxisName;
    }

    /**
     * Sets the value of the xAxisName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setXAxisName(String value) {
        this.xAxisName = value;
    }

    /**
     * Gets the value of the xSeries property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getXSeries() {
        return xSeries;
    }

    /**
     * Sets the value of the xSeries property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setXSeries(String value) {
        this.xSeries = value;
    }

    /**
     * Gets the value of the yAxisName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getYAxisName() {
        return yAxisName;
    }

    /**
     * Sets the value of the yAxisName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setYAxisName(String value) {
        this.yAxisName = value;
    }

    /**
     * Gets the value of the ySeries property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getYSeries() {
        return ySeries;
    }

    /**
     * Sets the value of the ySeries property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setYSeries(String value) {
        this.ySeries = value;
    }

    /**
     * Gets the value of the ySeriesLabel property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getYSeriesLabel() {
        return ySeriesLabel;
    }

    /**
     * Sets the value of the ySeriesLabel property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setYSeriesLabel(String value) {
        this.ySeriesLabel = value;
    }

    /**
     * Gets the value of the secondYAxisName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSecondYAxisName() {
        return secondYAxisName;
    }

    /**
     * Sets the value of the secondYAxisName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSecondYAxisName(String value) {
        this.secondYAxisName = value;
    }

    /**
     * Gets the value of the secondYSeries property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSecondYSeries() {
        return secondYSeries;
    }

    /**
     * Sets the value of the secondYSeries property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSecondYSeries(String value) {
        this.secondYSeries = value;
    }

    /**
     * Gets the value of the secondYSeriesLabel property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSecondYSeriesLabel() {
        return secondYSeriesLabel;
    }

    /**
     * Sets the value of the secondYSeriesLabel property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSecondYSeriesLabel(String value) {
        this.secondYSeriesLabel = value;
    }

    /**
     * Gets the value of the title property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTitle(String value) {
        this.title = value;
    }

    /**
     * Gets the value of the width property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getWidth() {
        return width;
    }

    /**
     * Sets the value of the width property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setWidth(Integer value) {
        this.width = value;
    }

    /**
     * Gets the value of the height property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getHeight() {
        return height;
    }

    /**
     * Sets the value of the height property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setHeight(Integer value) {
        this.height = value;
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
     * Gets the value of the datasetSize property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getDatasetSize() {
        return datasetSize;
    }

    /**
     * Sets the value of the datasetSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setDatasetSize(Integer value) {
        this.datasetSize = value;
    }

    /**
     * Gets the value of the titleFontSize property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getTitleFontSize() {
        return titleFontSize;
    }

    /**
     * Sets the value of the titleFontSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setTitleFontSize(Integer value) {
        this.titleFontSize = value;
    }

    /**
     * Gets the value of the labelFontSize property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getLabelFontSize() {
        return labelFontSize;
    }

    /**
     * Sets the value of the labelFontSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setLabelFontSize(Integer value) {
        this.labelFontSize = value;
    }

}
