
package com.intel.ca360.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for VirtualUserConfigType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="VirtualUserConfigType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="TotalUsers" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="StartIndex" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;choice>
 *           &lt;element name="FixedCredential" type="{http://www.intel.com/loadmeter/config}FixedCredentialType"/>
 *           &lt;element name="IndexedCredential" type="{http://www.intel.com/loadmeter/config}IndexedCredentialType"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VirtualUserConfigType", propOrder = {
    "totalUsers",
    "startIndex",
    "fixedCredential",
    "indexedCredential"
})
public class VirtualUserConfigType {

    @XmlElement(name = "TotalUsers")
    protected int totalUsers;
    @XmlElement(name = "StartIndex")
    protected int startIndex;
    @XmlElement(name = "FixedCredential")
    protected FixedCredentialType fixedCredential;
    @XmlElement(name = "IndexedCredential")
    protected IndexedCredentialType indexedCredential;

    /**
     * Gets the value of the totalUsers property.
     * 
     */
    public int getTotalUsers() {
        return totalUsers;
    }

    /**
     * Sets the value of the totalUsers property.
     * 
     */
    public void setTotalUsers(int value) {
        this.totalUsers = value;
    }

    /**
     * Gets the value of the startIndex property.
     * 
     */
    public int getStartIndex() {
        return startIndex;
    }

    /**
     * Sets the value of the startIndex property.
     * 
     */
    public void setStartIndex(int value) {
        this.startIndex = value;
    }

    /**
     * Gets the value of the fixedCredential property.
     * 
     * @return
     *     possible object is
     *     {@link FixedCredentialType }
     *     
     */
    public FixedCredentialType getFixedCredential() {
        return fixedCredential;
    }

    /**
     * Sets the value of the fixedCredential property.
     * 
     * @param value
     *     allowed object is
     *     {@link FixedCredentialType }
     *     
     */
    public void setFixedCredential(FixedCredentialType value) {
        this.fixedCredential = value;
    }

    /**
     * Gets the value of the indexedCredential property.
     * 
     * @return
     *     possible object is
     *     {@link IndexedCredentialType }
     *     
     */
    public IndexedCredentialType getIndexedCredential() {
        return indexedCredential;
    }

    /**
     * Sets the value of the indexedCredential property.
     * 
     * @param value
     *     allowed object is
     *     {@link IndexedCredentialType }
     *     
     */
    public void setIndexedCredential(IndexedCredentialType value) {
        this.indexedCredential = value;
    }

}
