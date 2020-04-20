package com.intel.cedar.engine.xml.model;

import com.intel.cedar.engine.xml.Configuration;
import com.intel.cedar.engine.xml.NamePool;
import com.intel.cedar.engine.xml.StructuredQName;
import com.intel.cedar.engine.xml.XMLException;
import com.intel.cedar.engine.xml.iterator.AxisIterator;
import com.intel.cedar.engine.xml.iterator.NodeTest;

/**
 * The Node interface represents a node in Saxon's implementation of the XPath
 * 2.0 data model.
 * <p>
 * Note that several Node objects may represent the same node. To test node
 * identity, the method {@link #isSameNode(Node)} should be used. An exception
 * to this rule applies for document nodes, where the correspondence between
 * document nodes and Document objects is one to one. Node objects are never
 * reused: a given Node object represents the same node for its entire lifetime.
 * <p>
 * This is the primary interface for accessing trees in Saxon, and it forms part
 * of the public Saxon API. The only subclass of Node that applications should
 * normally use is {@link Document}, which represents a document node. Methods
 * that form part of the public API are (since Saxon 8.4) labelled with a
 * JavaDoc "since" tag: classes and methods that have no such label should not
 * be regarded as stable interfaces.
 * <p>
 * The interface represented by this class is at a slightly higher level than
 * the abstraction described in the W3C data model specification, in that it
 * includes support for the XPath axes, rather than exposing the lower-level
 * properties (such as "parent" and "children") directly. All navigation within
 * trees, except for a few convenience methods, is done by following the axes
 * using the {@link #iterateAxis} method. This allows different implementations
 * of the XPath tree model to implement axis navigation in different ways. Some
 * implementations may choose to use the helper methods provided in class
 * {@link Navigator}.
 * <p>
 * Note that the stability of this interface applies to classes that use the
 * interface, not to classes that implement it. The interface may be extended in
 * future to add new methods.
 * <p>
 * New implementations of Node are advised also to implement the methods in
 * interface ExtendedNode, which will be moved into this interface at some time
 * in the future.
 * 
 * @author Michael H. Kay
 * @since 8.4
 */

public interface Node {

    final static int[] EMPTY_NAMESPACE_LIST = new int[0];

    /**
     * Type representing an element node - element()
     */

    public static final short ELEMENT = 1;
    /**
     * Item type representing an attribute node - attribute()
     */
    public static final short ATTRIBUTE = 2;
    /**
     * Item type representing a text node - text()
     */
    public static final short TEXT = 3;
    /**
     * Item type representing a processing-instruction node
     */
    public static final short PROCESSING_INSTRUCTION = 7;
    /**
     * Item type representing a comment node
     */
    public static final short COMMENT = 8;
    /**
     * Item type representing a document node
     */
    public static final short DOCUMENT = 9;
    /**
     * Item type representing a namespace node
     */
    public static final short NAMESPACE = 13;

    /**
     * An item type that matches any node
     */

    public static final short NODE = 0;

    /**
     * Bit setting in the returned type annotation indicating a DTD_derived type
     * on an attribute node
     */

    public static int IS_DTD_TYPE = 1 << 30;

    /**
     * Bit setting for use alongside a type annotation indicating that the
     * is-nilled property is set
     */

    public static int IS_NILLED = 1 << 29;

    /**
     * Get the kind of node. This will be a value such as
     * {@link net.sf.saxon.type.Type#ELEMENT} or
     * {@link net.sf.saxon.type.Type#ATTRIBUTE}. There are seven kinds of node:
     * documents, elements, attributes, text, comments, processing-instructions,
     * and namespaces.
     * 
     * @return an integer identifying the kind of node. These integer values are
     *         the same as those used in the DOM
     * @see net.sf.saxon.type.Type
     * @since 8.4
     */

    public int getNodeKind();

    /**
     * Determine whether this is the same node as another node.
     * <p>
     * Note that two different Node instances can represent the same conceptual
     * node. Therefore the "==" operator should not be used to test node
     * identity. The equals() method should give the same result as
     * isSameNode(), but since this rule was introduced late it might not apply
     * to all implementations.
     * <p>
     * Note: a.isSameNode(b) if and only if generateId(a)==generateId(b).
     * <p>
     * This method has the same semantics as isSameNode() in DOM Level 3, but
     * works on Saxon Node objects rather than DOM Node objects.
     * 
     * @param other
     *            the node to be compared with this node
     * @return true if this Node object and the supplied Node object represent
     *         the same node in the tree.
     */

    public boolean isSameNode(Node other);

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

    public boolean equals(Object other);

    /**
     * Get the System ID for the node. Note this is not the same as the base
     * URI: the base URI can be modified by xml:base, but the system ID cannot.
     * The base URI is used primarily for resolving relative URIs within the
     * content of the document. The system ID is used primarily in conjunction
     * with a line number, for identifying the location of elements within the
     * source XML, in particular when errors are found. For a document node, the
     * System ID represents the value of the document-uri property as defined in
     * the XDM data model.
     * 
     * @return the System Identifier of the entity in the source document
     *         containing the node, or null if not known or not applicable.
     * @since 8.4
     */

    public String getSystemId();

    /**
     * Set the System ID for the node.
     */
    public void setSystemId(String uri);

    /**
     * Get the Base URI for the node, that is, the URI used for resolving a
     * relative URI contained in the node. This will be the same as the System
     * ID unless xml:base has been used. Where the node does not have a base URI
     * of its own, the base URI of its parent node is returned.
     * 
     * @return the base URI of the node. This may be null if the base URI is
     *         unknown.
     * @since 8.4
     */

    public String getBaseURI();

    /**
     * Get line number. Line numbers are not maintained by default, except for
     * stylesheets and schema documents. Line numbering can be requested using
     * the -l option on the command line, or by setting options on the
     * TransformerFactory or the Configuration before the source document is
     * built.
     * <p>
     * The granularity of line numbering is normally the element level: for
     * other nodes such as text nodes and attributes, the line number of the
     * parent element will normally be returned.
     * <p>
     * In the case of a tree constructed by taking input from a SAX parser, the
     * line number will reflect the SAX rules: that is, the line number of an
     * element is the line number where the start tag ends. This may be a little
     * confusing where elements have many attributes spread over multiple lines,
     * or where single attributes (as can easily happen with XSLT 2.0
     * stylesheets) occupy several lines.
     * <p>
     * In the case of a tree constructed by a stylesheet or query, the line
     * number may reflect the line in the stylesheet or query that caused the
     * node to be constructed.
     * <p>
     * The line number can be read from within an XPath expression using the
     * Saxon extension function saxon:line-number()
     * 
     * @return the line number of the node in its original source document; or
     *         -1 if not available
     * @since 8.4
     */

    public int getLineNumber();

    /*
     * Set the line number of this node
     */
    public void setLineNumber(int lineNumber);

    /**
     * Return the node value of the node according to its type.
     * 
     */
    public String getNodeValue();

    /**
     * Return the string value of the node as defined in the XPath data model.
     * <p>
     * The interpretation of this depends on the type of node. For an element it
     * is the accumulated character content of the element, including descendant
     * elements.
     * <p>
     * This method returns the string value as if the node were untyped. Unlike
     * the string value accessor in the XPath 2.0 data model, it does not report
     * an error if the element has a complex type, instead it returns the
     * concatenation of the descendant text nodes as it would if the element
     * were untyped.
     * 
     * @return the string value of the node
     * @since 8.4
     */

    public String getStringValue();

    /**
     * Get the string value of the item as a CharSequence. This is in some cases
     * more efficient than the version of the method that returns a String. The
     * method satisfies the rule that
     * <code>X.getStringValueCS().toString()</code> returns a string that is
     * equal to <code>X.getStringValue()</code>.
     * <p>
     * Note that two CharSequence values of different types should not be
     * compared using equals(), and for the same reason they should not be used
     * as a key in a hash table.
     * <p>
     * If the calling code can handle any CharSequence, this method should be
     * used. If the caller requires a string, the {@link #getStringValue} method
     * is preferred.
     * 
     * @return the string value of the item
     * @see #getStringValue
     * @since 8.4
     */

    public CharSequence getStringValueCS();

    /**
     * Get name code. The name code is a coded form of the node name: two nodes
     * with the same name code have the same namespace URI, the same local name,
     * and the same prefix. By masking the name code with
     * {@link NamePool#FP_MASK}, you get a fingerprint: two nodes with the same
     * fingerprint have the same local name and namespace URI.
     * 
     * @return an integer name code, which may be used to obtain the actual node
     *         name from the name pool. For unnamed nodes (text nodes, comments,
     *         document nodes, and namespace nodes for the default namespace),
     *         returns -1.
     * @see net.sf.saxon.om.NamePool#allocate allocate
     * @see net.sf.saxon.om.NamePool#getFingerprint getFingerprint
     * @since 8.4
     */

    public int getNameCode();

    /**
     * Get fingerprint. The fingerprint is a coded form of the expanded name of
     * the node: two nodes with the same name code have the same namespace URI
     * and the same local name. The fingerprint contains no information about
     * the namespace prefix. For a name in the null namespace, the fingerprint
     * is the same as the name code.
     * 
     * @return an integer fingerprint; two nodes with the same fingerprint have
     *         the same expanded QName. For unnamed nodes (text nodes, comments,
     *         document nodes, and namespace nodes for the default namespace),
     *         returns -1.
     * @since 8.4
     */

    public int getFingerprint();

    /*
     * Get the node name in StructuredQName
     */
    StructuredQName getNodeName();

    /**
     * Get the local part of the name of this node. This is the name after the
     * ":" if any.
     * 
     * @return the local part of the name. For an unnamed node, returns "".
     *         Unlike the DOM interface, this returns the full name in the case
     *         of a non-namespaced name.
     * @since 8.4
     */

    public String getLocalPart();

    /**
     * Get the URI part of the name of this node. This is the URI corresponding
     * to the prefix, or the URI of the default namespace if appropriate.
     * 
     * @return The URI of the namespace of this node. For an unnamed node, or
     *         for an element or attribute that is not in a namespace, or for a
     *         processing instruction, returns an empty string.
     * @since 8.4
     */

    public String getURI();

    /**
     * Get the display name of this node, in the form of a lexical QName. For
     * elements and attributes this is [prefix:]localname. For unnamed nodes, it
     * is an empty string.
     * 
     * @return The display name of this node. For a node with no name, returns
     *         an empty string.
     * @since 8.4
     */

    public String getDisplayName();

    /**
     * Get the prefix of the name of the node. This is defined only for elements
     * and attributes. If the node has no prefix, or for other kinds of node,
     * returns a zero-length string.
     * 
     * @return The prefix of the name of the node.
     * @since 8.4
     */

    public String getPrefix();

    /**
     * Get the configuration used to build the tree containing this node.
     * 
     * @return the Configuration
     * @since 8.4
     */

    public Configuration getConfiguration();

    /**
     * Get the NamePool that holds the namecode for this node
     * 
     * @return the namepool
     * @since 8.4
     */

    public NamePool getNamePool();

    /**
     * Get the type annotation of this node, if any. The type annotation is
     * represented as an integer; this is the fingerprint of the name of the
     * type, as defined in the name pool. Anonymous types are given a
     * system-defined name. The value of the type annotation can be used to
     * retrieve the actual schema type definition using the method
     * {@link Configuration#getSchemaType}.
     * <p>
     * The bit IS_DTD_TYPE (1<<30) will be set in the case of an attribute node
     * if the type annotation is one of ID, IDREF, or IDREFS and this is derived
     * from DTD rather than schema validation.
     * 
     * @return the type annotation of the node, under the mask NamePool.FP_MASK,
     *         and optionally the bit setting IS_DTD_TYPE in the case of a
     *         DTD-derived ID or IDREF/S type (which is treated as untypedAtomic
     *         for the purposes of obtaining the typed value).
     * 
     *         <p>
     *         The result is undefined for nodes other than elements and
     *         attributes.
     *         </p>
     * @since 8.4
     */

    public int getTypeAnnotation();

    /**
     * Get the Node object representing the parent of this node
     * 
     * @return the parent of this node; null if this node has no parent
     * @since 8.4
     */

    public Node getParentNode();

    /**
     * Get the previous sibling of the node
     * 
     * @return The previous sibling node. Returns null if the current node is
     *         the first child of its parent.
     */

    public Node getPreviousSibling();

    /**
     * Get next sibling node
     * 
     */

    public Node getNextSibling();

    /**
     * Get first child - default implementation used for leaf nodes
     * 
     */

    public Node getFirstChild();

    /**
     * Get last child - default implementation used for leaf nodes
     * 
     */

    public Node getLastChild();

    /**
     * Return an iteration over all the nodes reached by the given axis from
     * this node
     * 
     * @exception UnsupportedOperationException
     *                if the namespace axis is requested and this axis is not
     *                supported for this implementation.
     * @param axisNumber
     *            an integer identifying the axis; one of the constants defined
     *            in class {@link net.sf.saxon.om.Axis}
     * @return an AxisIterator that delivers the nodes reached by the axis in
     *         turn. The nodes are returned in axis order (document order for a
     *         forwards axis, reverse document order for a reverse axis).
     * @see net.sf.saxon.om.Axis
     * @since 8.4
     */

    public AxisIterator iterateAxis(byte axisNumber);

    /**
     * Return an iteration over all the nodes reached by the given axis from
     * this node that match a given NodeTest
     * 
     * @exception UnsupportedOperationException
     *                if the namespace axis is requested and this axis is not
     *                supported for this implementation.
     * @param axisNumber
     *            an integer identifying the axis; one of the constants defined
     *            in class {@link net.sf.saxon.om.Axis}
     * @param nodeTest
     *            A condition to be satisfied by the returned nodes; nodes that
     *            do not satisfy this condition are not included in the result
     * @return an AxisIterator that delivers the nodes reached by the axis in
     *         turn. The nodes are returned in axis order (document order for a
     *         forwards axis, reverse document order for a reverse axis).
     * @see net.sf.saxon.om.Axis
     * @since 8.4
     */

    public AxisIterator iterateAxis(byte axisNumber, NodeTest nodeTest);

    /**
     * Returns whether this node (if it is an element) has any attributes.
     */

    public boolean hasAttributes();

    /**
     * Get the attributes of this node. For node types without attribuets, it
     * returns null.
     * 
     * @return A AttributeCollection containing the attributes of this node (if
     *         it is an Element) or null otherwise.
     */
    public AttributeCollection getAttributes();

    /**
     * Get the string value of a given attribute of this node
     * 
     * @param fingerprint
     *            The fingerprint of the attribute name
     * @return the attribute value if it exists, or null if it does not exist.
     *         Always returns null if this node is not an element.
     * @since 8.4
     */

    public String getAttributeValue(int fingerprint);

    /**
     * Get the root node of the tree containing this node
     * 
     * @return the Node representing the top-level ancestor of this node. This
     *         will not necessarily be a document node. If this node has no
     *         parent, then the method returns this node.
     * @since 8.4
     */

    public Node getRoot();

    /**
     * Get the root node, if it is a document node.
     * 
     * @return the Document representing the containing document. If this node
     *         is part of a tree that does not have a document node as its root,
     *         returns null.
     * @since 8.4
     */

    public Document getOwnerDocument();

    /**
     * Determine whether the node has any children.
     * <p>
     * Note: the result is equivalent to <br />
     * <code>iterateAxis(Axis.CHILD).next() != null</code>
     * 
     * @return True if the node has one or more children
     * @since 8.4
     */

    public boolean hasChildNodes();

    /**
     * Get all namespace declarations and undeclarations defined on this
     * element.
     * <p>
     * This method is intended primarily for internal use. User applications
     * needing information about the namespace context of a node should use
     * <code>iterateAxis(Axis.NAMESPACE)</code>. (However, not all
     * implementations support the namespace axis, whereas all implementations
     * are required to support this method.)
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
     *         <p>
     *         For a node other than an element, the method returns null.
     *         </p>
     */

    public int[] getDeclaredNamespaces(int[] buffer);

    /**
     * Used to know read-only state of children.
     * 
     * @return boolean Whether children of the element can be appended or
     *         removed.
     */
    boolean isChildEditable();

    /**
     * Append a child node to this node. Note: normalizing adjacent text nodes
     * is the responsibility of the caller.
     * 
     * @param newChild
     *            the node to be added as a child of this node
     */

    public Node appendChild(Node newChild) throws XMLException;

    /**
     * Insert a child node before a reference child to this node.
     * 
     * @param newChild
     *            the node to be added as a child of this node
     * @param refChild
     *            the reference child before which to insert
     */
    public Node insertBefore(Node newChild, Node refChild) throws XMLException;

    /**
     * Removes the child node indicated by oldChild from the list of children,
     * and returns it.
     * 
     * @param oldChild
     *            the node to be removed
     */
    public Node removeChild(Node oldChild) throws XMLException;

    /**
     * Replaces the child node oldChild with newChild in the list of children,
     * and returns the oldChild node.
     * 
     */
    public Node replaceChild(Node newChild, Node oldChild) throws XMLException;

    /**
     * setNodeValue method
     * 
     * @param textvalue_2
     *            java.lang.String
     */
    public void setNodeValue(String nodeValue);

    /**
     * clone a new node with same type and data as this node
     * 
     */

    public Node cloneNode(boolean deep);
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
