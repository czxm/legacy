package com.intel.cedar.engine.xml.model;

import com.intel.cedar.engine.xml.document.IDOMModel;

/**
 * A node in the XML parse tree representing an attribute. Note that this is
 * generated only "on demand", when the attribute is selected by a select
 * pattern.
 * <P>
 * 
 * @author Michael H. Kay
 */

public class AttributeImpl extends NodeImpl implements Attribute {

    private int nameCode;
    private int typeCode;
    private int properties;
    private String value;

    public AttributeImpl(int nameCode, int typeCode, int properties,
            String value) {
        this.nameCode = nameCode;
        this.typeCode = typeCode;
        this.properties = properties;
        this.value = value;
    }

    public AttributeImpl(ElementImpl element, int nameCode, int typeCode,
            int properties, String value) {
        parent = element;
        this.nameCode = nameCode;
        this.typeCode = typeCode;
        this.properties = properties;
        this.value = value;
    }

    public AttributeImpl(DocumentImpl ownerDocument, ElementImpl element,
            int nameCode, int typeCode, int properties, String value) {
        super(ownerDocument);
        parent = element;
        this.nameCode = nameCode;
        this.typeCode = typeCode;
        this.properties = properties;
        this.value = value;
    }

    /**
     * AttrImpl constructor
     * 
     * @param that
     *            AttrImpl
     */
    protected AttributeImpl(AttributeImpl that) {
        super(that);

        if (that != null) {
            this.nameCode = that.nameCode;
            this.typeCode = that.typeCode;
            this.properties = that.properties;
            this.value = that.value;
        }
    }

    /**
     * cloneNode method
     * 
     * @return org.w3c.dom.Node
     */
    public Node cloneNode(boolean deep) {
        AttributeImpl cloned = new AttributeImpl(this);
        return cloned;
    }

    /**
     * Construct an Attribute node for the n'th attribute of a given element
     * 
     * @param element
     *            The element containing the relevant attribute
     * @param index
     *            The index position of the attribute starting at zero
     */

    public AttributeImpl(ElementImpl element, int index) {
        super((DocumentImpl) element.getOwnerDocument());
        parent = element;
        AttributeCollection atts = element.getAttributes();
        this.nameCode = atts.getNameCode(index);
        this.typeCode = atts.getTypeAnnotation(index);
        this.properties = atts.getProperties(index);
        this.value = atts.getValue(index);
    }

    /**
     * Get the owner element which contains the attribute
     */

    public Element getOwnerElement() {
        return (Element) getParentNode();
    }

    /**
     * Set the owner element which contains the attribute
     */

    public void setOwnerElement(ElementImpl ownerElement) {
        setParentNode(ownerElement);
    }

    /**
     * Get the name code, which enables the name to be located in the name pool
     */

    public int getNameCode() {
        return nameCode;
    }

    /**
     * Get the type annotation of this node, if any
     */

    public int getTypeAnnotation() {
        return typeCode;
    }

    /**
     * Get the type annotation of this node, if any
     */

    public int getProperties() {
        return properties;
    }

    /**
     * Determine whether this node has the is-id property
     * 
     * @return true if the node is an ID
     */

    public boolean isId() {
        return false;
    }

    /**
     * Determine whether the node has the is-nilled property
     * 
     * @return true if the node has the is-nilled property
     */

    public boolean isNilled() {
        return false;
    }

    /**
     * Determine whether this is the same node as another node
     * 
     * @return true if this Node object and the supplied Node object represent
     *         the same node in the tree.
     */

    public boolean isSameNode(Node other) {
        if (!(other instanceof AttributeImpl))
            return false;
        if (this == other)
            return true;
        AttributeImpl otherAtt = (AttributeImpl) other;
        return (parent.isSameNode(otherAtt.parent) && ((nameCode & 0xfffff) == (otherAtt.nameCode & 0xfffff)));
    }

    /**
     * Return the type of node.
     * 
     * @return Node.ATTRIBUTE
     */

    public final int getNodeKind() {
        return Node.ATTRIBUTE;
    }

    /**
     * Return the character value of the node.
     * 
     * @return the attribute value
     */

    public String getStringValue() {
        return value;
    }

    /**
     * Return the node value of the node according to its type.
     * 
     */
    public String getNodeValue() {
        return getValue();
    }

    /**
     * Get next sibling - not defined for attributes
     */

    public Node getNextSibling() {
        return null;
    }

    /**
     * Get previous sibling - not defined for attributes
     */

    public Node getPreviousSibling() {
        return null;
    }

    /**
     * Get the previous node in document order (skipping attributes)
     */

    public NodeImpl getPreviousInDocument() {
        return (NodeImpl) getParentNode();
    }

    /**
     * Get the next node in document order (skipping attributes)
     */

    public NodeImpl getNextInDocument(NodeImpl anchor) {
        if (anchor == this)
            return null;
        return ((NodeImpl) getParentNode()).getNextInDocument(anchor);
    }

    /**
     * Get the value of the attribute is returned as a string.
     * 
     * @return the value of the attribute
     */
    public String getValue() {
        return value;
    }

    /**
     * Set the parent of the attribute node.
     * 
     * @return
     */
    public void setParent(ElementImpl parent) {
        this.parent = parent;
    }

    /**
     * Set the name code of the attribute node.
     * 
     * @return
     */
    public void setNameCode(int nameCode) {
        this.nameCode = nameCode;
    }

    /**
     * Set the type code of the attribute node.
     * 
     * @return
     */
    public void setTypeCode(int typeCode) {
        this.typeCode = typeCode;
    }

    /**
     * Set the properties of the attribute node.
     * 
     * @return
     */
    public void setProperties(int properties) {
        this.properties = properties;
    }

    /**
     * Set the value of the attribute node.
     * 
     * @return
     */
    public void setValue(String value) {
        this.value = value;

        notifyValueChanged();
    }

    /**
     * setNodeValue method
     * 
     * @param textvalue_2
     *            java.lang.String
     */
    public void setNodeValue(String nodeValue) {
        setValue(nodeValue);
    }

    /**
     * notifyValueChanged method
     */
    protected void notifyNameChanged() {
        if (this.parent == null)
            return;
        DocumentImpl document = (DocumentImpl) this.parent
                .getContainerDocument();
        if (document == null)
            return;
        IDOMModel model = (IDOMModel) document.getStructuredModel();
        if (model == null)
            return;
        model.nameChanged(this);
    }

    /**
     * notifyValueChanged method
     */
    protected void notifyValueChanged() {
        if (this.parent == null)
            return;
        DocumentImpl document = (DocumentImpl) this.parent
                .getContainerDocument();
        if (document == null)
            return;
        IDOMModel model = (IDOMModel) document.getStructuredModel();
        if (model == null)
            return;
        model.valueChanged(this);
    }
}

//
// The contents of this file are subject to the Mozilla Public License Version
// 1.0 (the "License");
// you may not use this file except in compliance with the License. You may
// obtain a copy of the
// License at http://www.mozilla.org/MPL/
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations
// under the License.
//
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is Michael H. Kay.
//
// Portions created by (your name) are Copyright (C) (your legal entity). All
// Rights Reserved.
//
// Contributor(s): none.
//
