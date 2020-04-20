
package com.intel.ca360.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for FilesConfigType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="FilesConfigType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element name="Files" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="FilePattern" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FilesConfigType", propOrder = {
    "files",
    "filePattern"
})
public class FilesConfigType {

    @XmlElement(name = "Files")
    protected String files;
    @XmlElement(name = "FilePattern")
    protected String filePattern;

    /**
     * Gets the value of the files property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFiles() {
        return files;
    }

    /**
     * Sets the value of the files property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFiles(String value) {
        this.files = value;
    }

    /**
     * Gets the value of the filePattern property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFilePattern() {
        return filePattern;
    }

    /**
     * Sets the value of the filePattern property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFilePattern(String value) {
        this.filePattern = value;
    }

}
