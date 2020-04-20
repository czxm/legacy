package com.intel.cedar.engine.xml.iterator;

import com.intel.cedar.engine.util.IntHashSet;
import com.intel.cedar.engine.xml.NamePool;
import com.intel.cedar.engine.xml.model.Node;
import com.intel.cedar.engine.xml.type.ItemType;
import com.intel.cedar.engine.xml.type.Type;
import com.intel.cedar.engine.xml.type.TypeHierarchy;

/**
 * NodeTest is an interface that enables a test of whether a node has a
 * particular name and Node. A NameTest matches the node kind and the namespace
 * URI and the local name.
 * 
 * @author Michael H. Kay
 */

public class NameTest extends NodeTest {

    private int nodeKind;
    private int fingerprint;
    private int nameCode;
    private NamePool namePool;
    private String uri = null;
    private String localName = null;

    /**
     * Create a NameTest to match nodes by name
     * 
     * @param nodeKind
     *            the kind of node, for example {@link Type#ELEMENT}
     * @param uri
     *            the namespace URI of the required nodes. Supply "" to match
     *            nodes that are in no namespace
     * @param localName
     *            the local name of the required nodes. Supply "" to match
     *            unnamed nodes
     * @param namePool
     *            the namePool holding the name codes
     * @since 9.0
     */

    public NameTest(int nodeKind, String prefix, String uri, String localName,
            NamePool namePool) {
        this.nodeKind = nodeKind;
        this.nameCode = namePool.allocate(prefix, uri, localName);
        this.fingerprint = this.nameCode & NamePool.FP_MASK;
        this.namePool = namePool;
    }

    /**
     * Create a NameTest to match nodes by their nameCode allocated from the
     * NamePool
     * 
     * @param nodeKind
     *            the kind of node, for example {@link Type#ELEMENT}
     * @param nameCode
     *            the nameCode representing the name of the node
     * @param namePool
     *            the namePool holding the name codes
     * @since 8.4
     */

    public NameTest(int nodeKind, int nameCode, NamePool namePool) {
        this.nodeKind = nodeKind;
        this.nameCode = nameCode;
        this.fingerprint = nameCode & 0xfffff;
        this.namePool = namePool;
    }

    /**
     * Create a NameTest for nodes of the same type and name as a given node
     */

    public NameTest(Node node) {
        this.nodeKind = node.getNodeKind();
        this.fingerprint = node.getFingerprint();
        this.nameCode = node.getNameCode();
        this.namePool = node.getNamePool();
    }

    public NameTest(int nodeKind) {
        this.nodeKind = nodeKind;
    }

    /**
     * Test whether this node test is satisfied by a given node
     * 
     * @param nodeKind
     *            The type of node to be matched
     * @param nameCode
     *            identifies the expanded name of the node to be matched
     */

    public boolean matches(int nodeKind, int nameCode, int annotation) {
        // System.err.println("Matching node " + nameCode + " against " +
        // this.fingerprint);
        // System.err.println("  " + ((nameCode&0xfffff) == this.fingerprint &&
        // nodeType == this.nodeType));
        return ((nameCode & 0xfffff) == this.fingerprint && nodeKind == this.nodeKind);
        // deliberately in this order for speed (first test usually fails)
    }

    /**
     * Test whether this node test is satisfied by a given node. This
     * alternative method is used in the case of nodes where calculating the
     * fingerprint is expensive, for example DOM or JDOM nodes.
     * 
     * @param node
     *            the node to be matched
     */

    public boolean matches(Node node) {
        if (node.getNodeKind() != nodeKind) {
            return false;
        }

        // Two different algorithms are used for name matching. If the
        // fingerprint of the node is readily
        // available, we use it to do an integer comparison. Otherwise, we do
        // string comparisons on the URI
        // and local name. In practice, Saxon's native node implementations use
        // fingerprint matching, while
        // DOM and JDOM nodes use string comparison of names

        return node.getFingerprint() == fingerprint;
    }

    /**
     * Determine the default priority of this node test when used on its own as
     * a Pattern
     */

    public final double getDefaultPriority() {
        return 0.0;
    }

    /**
     * Determine the types of nodes to which this pattern applies. Used for
     * optimisation. For patterns that match nodes of several types, return
     * Type.NODE
     * 
     * @return the type of node matched by this pattern. e.g. Type.ELEMENT or
     *         Type.TEXT
     */

    public int getPrimitiveType() {
        return nodeKind;
    }

    /**
     * Get the fingerprint required
     */
    public int getFingerprint() {
        return fingerprint;
    }

    /**
     * Get the namecode required
     */
    public int getNameCode() {
        return nameCode;
    }

    /**
     * Get the type from which this item type is derived by restriction. This is
     * the supertype in the XPath type heirarchy, as distinct from the Schema
     * base type: this means that the supertype of xs:boolean is
     * xs:anyAtomicType, whose supertype is item() (rather than
     * xs:anySimpleType).
     * <p>
     * In fact the concept of "supertype" is not really well-defined, because
     * the types form a lattice rather than a hierarchy. The only real
     * requirement on this function is that it returns a type that strictly
     * subsumes this type, ideally as narrowly as possible.
     * 
     * @return the supertype, or null if this type is item()
     * @param th
     *            the type hierarchy cache
     */

    public ItemType getSuperType(TypeHierarchy th) {
        return NodeKindTest.makeNodeKindTest(nodeKind);
    }

    /**
     * Get a mask indicating which kinds of nodes this NodeTest can match. This
     * is a combination of bits: 1<<Node.ELEMENT for element nodes, 1<<Node.TEXT
     * for text nodes, and so on.
     */

    public int getNodeKindMask() {
        return 1 << nodeKind;
    }

    /**
     * Get the set of node names allowed by this NodeTest. This is returned as a
     * set of Integer fingerprints. A null value indicates that all names are
     * permitted (i.e. that there are no constraints on the node name. The
     * default implementation returns null.
     */

    public IntHashSet getRequiredNodeNames() {
        IntHashSet s = new IntHashSet(1);
        s.add(fingerprint);
        return s;
    }

    public String toString() {
        return toString(namePool);
    }

    public String toString(int axis) {
        return toString(namePool, axis);
    }

    public String toString(NamePool pool) {
        if (null == pool)
            return "NodeKindTest:" + nodeKind;
        switch (nodeKind) {
        case Node.ELEMENT:
            return "element(" + pool.getDisplayName(nameCode) + ")";
        case Node.ATTRIBUTE:
            return "attribute(" + pool.getDisplayName(nameCode) + ")";
        case Node.PROCESSING_INSTRUCTION:
            return "processing-instruction(" + pool.getDisplayName(nameCode)
                    + ')';
        case Node.NAMESPACE:
            return "namespace(" + pool.getDisplayName(nameCode) + ')';
        }
        return pool.getDisplayName(nameCode);
    }

    public String toString(NamePool pool, int axis) {
        if (null == pool)
            return "";

        switch (nodeKind) {
        case Node.ELEMENT: {
            if (axis == Axis.ATTRIBUTE || axis == Axis.NAMESPACE)
                return "element(" + pool.getDisplayName(nameCode) + ")";
            break;
        }
        case Node.ATTRIBUTE: {
            if (axis != Axis.ATTRIBUTE)
                return "attribute(" + pool.getDisplayName(nameCode) + ")";
            break;
        }
        case Node.PROCESSING_INSTRUCTION:
            return "processing-instruction(" + pool.getDisplayName(nameCode)
                    + ')';
        case Node.NAMESPACE: {
            if (axis != Axis.NAMESPACE)
                return "namespace(" + pool.getDisplayName(nameCode) + ')';
            break;
        }
        }
        return pool.getDisplayName(nameCode);
    }

    /**
     * Returns a hash code value for the object.
     */

    public int hashCode() {
        return nodeKind << 20 ^ fingerprint;
    }

    /**
     * Determines whether two NameTests are equal
     */

    public boolean equals(Object other) {
        return other instanceof NameTest
                && ((NameTest) other).namePool == namePool
                && ((NameTest) other).nodeKind == nodeKind
                && ((NameTest) other).fingerprint == fingerprint;
    }

    public String getLocalPart() {
        return namePool.getLocalName(fingerprint);
    }

    public String getURI() {
        return namePool.getURI(fingerprint);
    }

    public int getNodeKind() {
        return nodeKind;
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
