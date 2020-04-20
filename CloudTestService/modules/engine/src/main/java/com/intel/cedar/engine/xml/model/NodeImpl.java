package com.intel.cedar.engine.xml.model;

import org.xml.sax.Locator;

import com.intel.cedar.engine.util.Navigator;
import com.intel.cedar.engine.xml.Configuration;
import com.intel.cedar.engine.xml.NamePool;
import com.intel.cedar.engine.xml.StandardNames;
import com.intel.cedar.engine.xml.StructuredQName;
import com.intel.cedar.engine.xml.XMLException;
import com.intel.cedar.engine.xml.document.IDOMModel;
import com.intel.cedar.engine.xml.document.IDocumentRegion;
import com.intel.cedar.engine.xml.document.IStructuredModel;
import com.intel.cedar.engine.xml.event.AbstractNotifier;
import com.intel.cedar.engine.xml.event.AdapterFactoryRegistry;
import com.intel.cedar.engine.xml.event.INodeNotifier;
import com.intel.cedar.engine.xml.iterator.AncestorIterator;
import com.intel.cedar.engine.xml.iterator.AnyNodeTest;
import com.intel.cedar.engine.xml.iterator.AttributeIterator;
import com.intel.cedar.engine.xml.iterator.Axis;
import com.intel.cedar.engine.xml.iterator.AxisIterator;
import com.intel.cedar.engine.xml.iterator.DescendantIterator;
import com.intel.cedar.engine.xml.iterator.EmptyIterator;
import com.intel.cedar.engine.xml.iterator.FollowingIterator;
import com.intel.cedar.engine.xml.iterator.FollowingSiblingIterator;
import com.intel.cedar.engine.xml.iterator.NameTest;
import com.intel.cedar.engine.xml.iterator.NamespaceIterator;
import com.intel.cedar.engine.xml.iterator.NodeTest;
import com.intel.cedar.engine.xml.iterator.PrecedingIterator;
import com.intel.cedar.engine.xml.iterator.PrecedingOrAncestorIterator;
import com.intel.cedar.engine.xml.iterator.PrecedingSiblingIterator;

/**
 * A node in the XML parse tree representing an XML element, character content,
 * or attribute.
 * <P>
 * This is the top-level class in the implementation class hierarchy; it
 * essentially contains all those methods that can be defined using other
 * primitive methods, without direct access to data.
 * 
 * @author Michael H. Kay
 */

public abstract class NodeImpl extends AbstractNotifier implements Node,
        INodeNotifier, IDocumentRegion, Locator {
    protected DocumentImpl ownerDocument;
    protected ParentNodeImpl parent;
    protected NodeImpl previousSibling;
    protected NodeImpl nextSibling;
    protected int lineNumber = -1;
    protected ILineNumberCallback lineNumberCallback;

    protected NodeImpl() {
    }

    /**
     * NodeImpl constructor
     * 
     * @param that
     *            NodeImpl
     */
    protected NodeImpl(NodeImpl that) {
        if (that != null) {
            this.ownerDocument = that.ownerDocument;
        }
    }

    protected NodeImpl(DocumentImpl ownerDocument) {
        this.ownerDocument = ownerDocument;
    }

    /**
     * Get the value of the item as a CharSequence. This is in some cases more
     * efficient than the version of the method that returns a String.
     */

    public CharSequence getStringValueCS() {
        return getStringValue();
    }

    /**
     * Return the node value of the node according to its type.
     * 
     */
    public String getNodeValue() {
        return null;
    }

    /**
     * Get the type annotation of this node, if any
     */

    public int getTypeAnnotation() {
        return StandardNames.XS_UNTYPED;
    }

    /**
     * Get the column number of the node. The default implementation returns -1,
     * meaning unknown
     */

    public int getColumnNumber() {
        return -1;
    }

    /**
     * Get the public identifier of the document entity containing this node.
     * The default implementation returns null, meaning unknown
     */

    public String getPublicId() {
        return null;
    }

    /**
     * Set the system ID of this node. This method is provided so that a Node
     * implements the javax.xml.transform.Source interface, allowing a node to
     * be used directly as the Source of a transformation
     */

    public void setSystemId(String uri) {
        // overridden in DocumentImpl and ElementImpl
        if (parent == null)
            return;

        parent.setSystemId(uri);
    }

    /**
     * Determine whether this is the same node as another node
     * 
     * @return true if this Node object and the supplied Node object represent
     *         the same node in the tree.
     */

    public boolean isSameNode(Node other) {
        // default implementation: differs for attribute and namespace nodes
        return this == other;
    }

    /**
     * The equals() method compares nodes for identity. It is defined to give
     * the same result as isSameNode().
     * 
     * @param other
     *            the node to be compared with this node
     * @return true if this Node object and the supplied Node object represent
     *         the same node in the tree.
     * @since 8.7 Previously, the effect of the equals() method was not defined.
     *        Callers should therefore be aware that third party implementations
     *        of the Node interface may not implement the correct semantics. It
     *        is safer to use isSameNode() for this reason. The equals() method
     *        has been defined because it is useful in contexts such as a Java
     *        Set or HashMap.
     */

    public boolean equals(Object other) {
        return other instanceof Node && isSameNode((Node) other);
    }

    /**
     * Get the nameCode of the node. This is used to locate the name in the
     * NamePool
     */

    public int getNameCode() {
        // default implementation: return -1 for an unnamed node
        return -1;
    }

    /**
     * Get the fingerprint of the node. This is used to compare whether two
     * nodes have equivalent names. Return -1 for a node with no name.
     */

    public int getFingerprint() {
        int nameCode = getNameCode();
        if (nameCode == -1) {
            return -1;
        }
        return nameCode & 0xfffff;
    }

    /**
     * Get the system ID for the node. Default implementation for child nodes.
     */

    public String getSystemId() {
        if (parent == null)
            return null;

        return parent.getSystemId();
    }

    /**
     * Get the base URI for the node. Default implementation for child nodes.
     */

    public String getBaseURI() {
        if (parent == null)
            return null;

        return parent.getBaseURI();
    }

    /**
     * Get the configuration
     */

    public Configuration getConfiguration() {
        return getOwnerDocument().getConfiguration();
    }

    /**
     * Get the NamePool
     */

    public NamePool getNamePool() {
        return getOwnerDocument().getNamePool();
    }

    /**
     * Get the prefix part of the name of this node. This is the name before the
     * ":" if any.
     * 
     * @return the prefix part of the name. For an unnamed node, return an empty
     *         string.
     */

    public String getPrefix() {
        int nameCode = getNameCode();
        if (nameCode == -1) {
            return "";
        }
        if (NamePool.getPrefixIndex(nameCode) == 0) {
            return "";
        }
        return getNamePool().getPrefix(nameCode);
    }

    /**
     * Get the URI part of the name of this node. This is the URI corresponding
     * to the prefix, or the URI of the default namespace if appropriate.
     * 
     * @return The URI of the namespace of this node. For the default namespace,
     *         return an empty string. For an unnamed node, return the empty
     *         string.
     */

    public String getURI() {
        int nameCode = getNameCode();
        if (nameCode == -1) {
            return "";
        }
        return getNamePool().getURI(nameCode);
    }

    /**
     * Get the display name of this node. For elements and attributes this is
     * [prefix:]localname. For unnamed nodes, it is an empty string.
     * 
     * @return The display name of this node. For a node with no name, return an
     *         empty string.
     */

    public String getDisplayName() {
        int nameCode = getNameCode();
        if (nameCode == -1) {
            return "";
        }
        return getNamePool().getDisplayName(nameCode);
    }

    /**
     * Get the local name of this node.
     * 
     * @return The local name of this node. For a node with no name, return "",.
     */

    public String getLocalPart() {
        int nameCode = getNameCode();
        if (nameCode == -1) {
            return "";
        }
        return getNamePool().getLocalName(nameCode);
    }

    public StructuredQName getNodeName() {
        return new StructuredQName(getPrefix(), getURI(), getLocalPart());
    }

    /**
     * Get the line number of the node within its source document entity
     */

    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Get the line number of the node within its source document entity
     */

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;

        if (lineNumberCallback != null)
            lineNumberCallback.setLineNumber(lineNumber);
    }

    public void setLineNumberCallback(ILineNumberCallback lineNumberCallback) {
        this.lineNumberCallback = lineNumberCallback;
    }

    /**
     * Find the parent node of this node.
     * 
     * @return The Node object describing the containing element or root node.
     */

    public final Node getParentNode() {
        return parent;
    }

    /**
     * Get the previous sibling of the node
     * 
     * @return The previous sibling node. Returns null if the current node is
     *         the first child of its parent.
     */

    public Node getPreviousSibling() {
        return previousSibling;
    }

    /**
     * Get next sibling node
     * 
     * @return The next sibling node of the required type. Returns null if the
     *         current node is the last child of its parent.
     */

    public Node getNextSibling() {
        return nextSibling;
    }

    /**
     * Get first child - default implementation used for leaf nodes
     * 
     * @return null
     */

    public Node getFirstChild() {
        return null;
    }

    /**
     * Get last child - default implementation used for leaf nodes
     * 
     * @return null
     */

    public Node getLastChild() {
        return null;
    }

    /**
     * Return an enumeration over the nodes reached by the given axis from this
     * node
     * 
     * @param axisNumber
     *            The axis to be iterated over
     * @return an AxisIterator that scans the nodes reached by the axis in turn.
     */

    public AxisIterator iterateAxis(byte axisNumber) {
        // Fast path for child axis
        if (axisNumber == Axis.CHILD) {
            if (this instanceof ParentNodeImpl) {
                return ((ParentNodeImpl) this).enumerateChildren(null);
            } else {
                return EmptyIterator.getInstance();
            }
        } else {
            return iterateAxis(axisNumber, AnyNodeTest.getInstance());
        }
    }

    /**
     * Return an enumeration over the nodes reached by the given axis from this
     * node
     * 
     * @param axisNumber
     *            The axis to be iterated over
     * @param nodeTest
     *            A pattern to be matched by the returned nodes
     * @return an AxisIterator that scans the nodes reached by the axis in turn.
     */

    public AxisIterator iterateAxis(byte axisNumber, NodeTest nodeTest) {

        switch (axisNumber) {
        case Axis.ANCESTOR:
            return new AncestorIterator(this, nodeTest, false);

        case Axis.ANCESTOR_OR_SELF:
            return new AncestorIterator(this, nodeTest, true);

        case Axis.ATTRIBUTE:
            if (getNodeKind() != Node.ELEMENT) {
                return EmptyIterator.getInstance();
            }
            return new AttributeIterator(this, nodeTest);

        case Axis.CHILD:
            if (this instanceof ParentNodeImpl) {
                return ((ParentNodeImpl) this).enumerateChildren(nodeTest);
            } else {
                return EmptyIterator.getInstance();
            }

        case Axis.DESCENDANT:
            if (getNodeKind() == Node.DOCUMENT && nodeTest instanceof NameTest
                    && nodeTest.getPrimitiveType() == Node.ELEMENT) {
                return ((DocumentImpl) this).getAllElements(nodeTest
                        .getFingerprint());
            } else if (hasChildNodes()) {
                return new DescendantIterator(this, nodeTest, false);
            } else {
                return EmptyIterator.getInstance();
            }

        case Axis.DESCENDANT_OR_SELF:
            return new DescendantIterator(this, nodeTest, true);

        case Axis.FOLLOWING:
            return new FollowingIterator(this, nodeTest);

        case Axis.FOLLOWING_SIBLING:
            return new FollowingSiblingIterator(this, nodeTest);

        case Axis.NAMESPACE:
            if (getNodeKind() != Node.ELEMENT) {
                return EmptyIterator.getInstance();
            }
            return NamespaceIterator.makeIterator(this, nodeTest);

        case Axis.PARENT:
            Node parent = getParentNode();
            if (parent == null) {
                return EmptyIterator.getInstance();
            }
            return Navigator.filteredSingleton(parent, nodeTest);

        case Axis.PRECEDING:
            return new PrecedingIterator(this, nodeTest);

        case Axis.PRECEDING_SIBLING:
            return new PrecedingSiblingIterator(this, nodeTest);

        case Axis.SELF:
            return Navigator.filteredSingleton(this, nodeTest);

        case Axis.PRECEDING_OR_ANCESTOR:
            return new PrecedingOrAncestorIterator(this, nodeTest);

        default:
            throw new IllegalArgumentException("Unknown axis number "
                    + axisNumber);
        }
    }

    /**
     * Returns whether this node (if it is an element) has any attributes.
     */

    public boolean hasAttributes() {
        return false;
    }

    /**
     * Get the attributes of this node. For node types without attribuets, it
     * returns null.
     * 
     * @return A AttributeCollection containing the attributes of this node (if
     *         it is an Element) or null otherwise.
     */
    public AttributeCollection getAttributes() {
        return null;
    }

    /**
     * Get the value of a given attribute of this node
     * 
     * @param fingerprint
     *            The fingerprint of the attribute name
     * @return the attribute value if it exists or null if not
     */

    public String getAttributeValue(int fingerprint) {
        return null;
    }

    /**
     * Get the root node
     * 
     * @return the Node representing the containing document
     */

    public Node getRoot() {
        return getOwnerDocument();
    }

    /**
     * Get the root (document) node
     * 
     * @return the Document representing the containing document
     */

    public Document getOwnerDocument() {
        return ownerDocument;
    }

    /**
     * Get the next node in document order
     * 
     * @param anchor
     *            the scan stops when it reaches a node that is not a descendant
     *            of the specified anchor node
     * @return the next node in the document, or null if there is no such node
     */

    public NodeImpl getNextInDocument(NodeImpl anchor) {
        // find the first child node if there is one; otherwise the next sibling
        // node
        // if there is one; otherwise the next sibling of the parent,
        // grandparent, etc, up to the anchor element.
        // If this yields no result, return null.

        NodeImpl next = (NodeImpl) getFirstChild();
        if (next != null) {
            return next;
        }
        if (this == anchor) {
            return null;
        }
        next = (NodeImpl) getNextSibling();
        if (next != null) {
            return next;
        }
        NodeImpl parent = this;
        while (true) {
            parent = (NodeImpl) parent.getParentNode();
            if (parent == null) {
                return null;
            }
            if (parent == anchor) {
                return null;
            }
            next = (NodeImpl) parent.getNextSibling();
            if (next != null) {
                return next;
            }
        }
    }

    /**
     * Get the previous node in document order
     * 
     * @return the previous node in the document, or null if there is no such
     *         node
     */

    public NodeImpl getPreviousInDocument() {

        // finds the last child of the previous sibling if there is one;
        // otherwise the previous sibling element if there is one;
        // otherwise the parent, up to the anchor element.
        // If this reaches the document root, return null.

        NodeImpl prev = (NodeImpl) getPreviousSibling();
        if (prev != null) {
            return prev.getLastDescendantOrSelf();
        }
        return (NodeImpl) getParentNode();
    }

    private NodeImpl getLastDescendantOrSelf() {
        NodeImpl last = (NodeImpl) getLastChild();
        if (last == null) {
            return this;
        }
        return last.getLastDescendantOrSelf();
    }

    /**
     * getCommonAncestor method
     * 
     * @return org.w3c.dom.Node
     * @param node
     *            org.w3c.dom.Node
     */
    public Node getCommonAncestor(Node node) {
        if (node == null)
            return null;

        for (Node na = node; na != null; na = na.getParentNode()) {
            for (Node ta = this; ta != null; ta = ta.getParentNode()) {
                if (ta == na)
                    return ta;
            }
        }

        return null; // not found
    }

    /**
     * getContainerDocument method
     * 
     * @return org.w3c.dom.Document
     */
    public Document getContainerDocument() {
        for (Node node = this; node != null; node = node.getParentNode()) {
            if (node.getNodeKind() == Node.DOCUMENT) {
                return (Document) node;
            }
        }
        return null;
    }

    /**
     * Get all namespace undeclarations and undeclarations defined on this
     * element.
     * 
     * @param buffer
     *            If this is non-null, and the result array fits in this buffer,
     *            then the result may overwrite the contents of this array, to
     *            avoid the cost of allocating a new array on the heap.
     * @return An array of integers representing the namespace declarations and
     *         undeclarations present on this element. For a node other than an
     *         element, return null. Otherwise, the returned array is a sequence
     *         of namespace codes, whose meaning may be interpreted by reference
     *         to the name pool. The top half word of each namespace code
     *         represents the prefix, the bottom half represents the URI. If the
     *         bottom half is zero, then this is a namespace undeclaration
     *         rather than a declaration. The XML namespace is never included in
     *         the list. If the supplied array is larger than required, then the
     *         first unused entry will be set to -1.
     *         <p/>
     *         <p>
     *         For a node other than an element, the method returns null.
     *         </p>
     */

    public int[] getDeclaredNamespaces(int[] buffer) {
        return null;
    }

    // implement DOM Node methods

    /**
     * Determine whether the node has any children.
     * 
     * @return <code>true</code> if the node has any children,
     *         <code>false</code> if the node has no children.
     */

    public boolean hasChildNodes() {
        return getFirstChild() != null;
    }

    /**
     * Can be used to test if the indexed regions contains the test position.
     * 
     * @param testPosition
     * @return true if test position is greater than or equal to start offset
     *         and less than start offset plus length.
     */
    public boolean contains(int testPosition) {
        return false;
    }

    /**
     * Can be used to get end offset of source text, relative to beginning of
     * documnt. Implementers should return -1 if, or some reason, the region is
     * not valid.
     * 
     * @return endoffset
     */
    public int getEndOffset() {
        return -1;
    }

    /**
     * Can be used to get source postion of beginning of indexed region.
     * Implementers should return -1 if, or some reason, the region is not
     * valid.
     * 
     * @return int position of start of index region.
     */
    public int getStartOffset() {
        return -1;
    }

    /**
     * Can be used to get the length of the source text. Implementers should
     * return -1 if, or some reason, the region is not valid.
     * 
     * @return int position of length of index region.
     */
    public int getLength() {
        return -1;
    }

    public AdapterFactoryRegistry getFactoryRegistry() {
        IStructuredModel model = getStructuredModel();
        if (model != null) {
            AdapterFactoryRegistry reg = model.getFactoryRegistry();
            if (reg != null)
                return reg;
        }
        return null;
    }

    /**
     * Returns the model associated with this node. Returns null if not part of
     * an active model.
     * 
     * @return IStructuredModel - returns the IStructuredModel this node is part
     *         of.
     */
    IDOMModel getStructuredModel() {
        DocumentImpl ownerDocument = (DocumentImpl) getOwnerDocument();
        if (ownerDocument == null)
            return null;
        return ownerDocument.getStructuredModel();
    }

    /**
     * Used to know read-only state of children.
     * 
     * @return boolean Whether children of the element can be appended or
     *         removed.
     */
    public boolean isChildEditable() {
        return false;
    }

    /**
     * Set the parent of the node, the caller must keep the integrity of other
     * links or data
     * 
     */
    protected void setOwnerDocument(Document ownerDocument) {
        this.ownerDocument = (DocumentImpl) ownerDocument;
    }

    /**
     * Set the parent of the node, the caller must keep the integrity of other
     * links or data
     * 
     */
    protected void setParentNode(Node parent) {
        this.parent = (ParentNodeImpl) parent;
    }

    /**
     * Set the previous sibling of the node
     * 
     */

    public void setPreviousSibling(Node previousSibling) {
        this.previousSibling = (NodeImpl) previousSibling;
    }

    /**
     * Set next sibling node
     */

    public void setNextSibling(Node nextSibling) {
        this.nextSibling = (NodeImpl) nextSibling;
    }

    /**
     * Append a child node to this node. Note: normalizing adjacent text nodes
     * is the responsibility of the caller.
     * 
     * @param newChild
     *            the node to be added as a child of this node
     */

    public Node appendChild(Node newChild) throws XMLException {
        // default not support
        throw new XMLException("operation not supported.");
    }

    /**
     * Insert a child node before a reference child to this node.
     * 
     * @param newChild
     *            the node to be added as a child of this node
     * @param refChild
     *            the reference child before which to insert
     */
    public Node insertBefore(Node newChild, Node refChild) throws XMLException {
        // default not support
        throw new XMLException("operation not supported.");
    }

    /**
     * Removes the child node indicated by oldChild from the list of children,
     * and returns it.
     * 
     * @param oldChild
     *            the node to be removed
     */
    public Node removeChild(Node oldChild) throws XMLException {
        // default not support
        throw new XMLException("operation not supported.");
    }

    /**
     * Replaces the child node oldChild with newChild in the list of children,
     * and returns the oldChild node.
     * 
     */
    public Node replaceChild(Node newChild, Node oldChild) throws XMLException {
        throw new XMLException("operation not supported.");
    }

    /**
     * setNodeValue method
     * 
     * @param textvalue_2
     *            java.lang.String
     */
    public void setNodeValue(String nodeValue) {
    }

    /**
     * isValid method check if the node is not removed
     * 
     * @return false if the node is in a removed subtree
     */
    public boolean isValid() {
        Node parent = getParentNode();
        while (null != parent && !parent.equals(this.getOwnerDocument())) {
            parent = parent.getParentNode();
        }
        if (null == parent)
            return false;
        else
            return true;
    }

    /**
     * notifyValueChanged method
     */
    protected void notifyValueChanged() {
        DocumentImpl document = (DocumentImpl) getContainerDocument();
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
