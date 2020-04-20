package com.intel.cedar.engine.xml.util;

/**
 * Options for basic dom serialization
 */

public class SerializeOptions {
    private boolean isRootDefaultNamespace = true;
    private boolean pretty = true;
    private boolean outputXMLHeader = true;
    private boolean escapeEntity = true;
    private boolean processPI = true;
    private boolean forceLongForm = false;
    private String lineSeperator = "\n";
    private String indent = "    ";
    private String xmlEncoding = "UTF-8";

    /**
     * Constructor
     */
    public SerializeOptions() {
    }

    /**
     * Returns whether do pretty printing
     * 
     * @return pretty printing if true
     */
    public boolean isPretty() {
        return pretty;
    }

    /**
     * Sets whether to do pretty printing
     * 
     * @param Specify
     *            true if pretty printing
     */
    public void setPretty(boolean pretty) {
        this.pretty = pretty;
    }

    /**
     * Returns whether output XML header
     * 
     * @return true if output XML header
     */
    public boolean isOutputXMLHeader() {
        return outputXMLHeader;
    }

    /**
     * Sets whether output XML header
     * 
     * @param Specify
     *            true if output XML header
     */
    public void setOutputXMLHeader(boolean outputXMLHeader) {
        this.outputXMLHeader = outputXMLHeader;
    }

    /**
     * Returns whether escape entity
     * 
     * @return true if escape entity
     */
    public boolean isEscapeEntity() {
        return escapeEntity;
    }

    /**
     * Sets whether escape entity
     * 
     * @param Specify
     *            true if escape entity
     */
    public void setEscapeEntity(boolean escapeEntity) {
        this.escapeEntity = escapeEntity;
    }

    /**
     * Returns whether process processing instruction
     * 
     * @return true if process processing instruction
     */
    public boolean isProcessPI() {
        return processPI;
    }

    /**
     * Sets whether process processing instruction
     * 
     * @param Specify
     *            true if process processing instruction
     */
    public void setProcessPI(boolean processPI) {
        this.processPI = processPI;
    }

    /**
     * Returns whether force output in long form for element
     * 
     * @return true if force output in long form for element
     */
    public boolean isForceLongForm() {
        return forceLongForm;
    }

    /**
     * Sets whether force output in long form for element
     * 
     * @param Specify
     *            true force output in long form for element
     */
    public void setForceLongForm(boolean forceLongForm) {
        this.forceLongForm = forceLongForm;
    }

    /**
     * Returns a specific line separator to use. The default is the line
     * separator (<tt>\n</tt>). A string is returned to support double codes (CR
     * + LF).
     * 
     * @return The specified line separator
     */
    public String getLineSeparator() {
        return lineSeperator;
    }

    /**
     * Sets the line separator. The default is the line separator (<tt>\n</tt>).
     * The machine's line separator can be obtained from the system property
     * <tt>line.separator</tt>, but is only useful if the document is edited on
     * machines of the same type. For general documents, use the Web line
     * separator.
     * 
     * @param lineSeparator
     *            The specified line separator
     */
    public void setLineSeparator(String lineSeparator) {
        if (lineSeparator == null)
            this.lineSeperator = "\n";
        else
            this.lineSeperator = lineSeparator;
    }

    /**
     * Returns indent string
     * 
     * @return The specified indent string
     */
    public String getIndent() {
        return indent;
    }

    /**
     * Sets the indent string
     * 
     * @param indent
     *            The specified indent string
     */
    public void setIndent(String indent) {
        if (indent == null)
            this.indent = "    ";
        else
            this.indent = indent;
    }

    /**
     * Returns XML encoding
     * 
     * @return The specified XML encoding
     */
    public String getXMLEncoding() {
        return xmlEncoding;
    }

    /**
     * Sets the XML encoding
     * 
     * @param indent
     *            The specified XML encoding
     */
    public void setXMLEncoding(String xmlEncoding) {
        if (xmlEncoding == null)
            this.xmlEncoding = "UTF-8";
        else
            this.xmlEncoding = xmlEncoding;
    }

    /**
     * Returns whether root default namespace
     * 
     * @return true if output root default namespace
     */
    public boolean isRootDefaultNamespace() {
        return isRootDefaultNamespace;
    }

    /**
     * Sets whether output root default namespace
     * 
     * @param Specify
     *            true if output root default namespace
     */
    public void setRootDefaultNamespace(boolean isRootDefaultNamespace) {
        this.isRootDefaultNamespace = isRootDefaultNamespace;
    }
}
