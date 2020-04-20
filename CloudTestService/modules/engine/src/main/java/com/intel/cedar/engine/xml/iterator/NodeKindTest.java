package com.intel.cedar.engine.xml.iterator;

import com.intel.cedar.engine.xml.model.Node;
import com.intel.cedar.engine.xml.type.AnySimpleType;
import com.intel.cedar.engine.xml.type.AnyType;
import com.intel.cedar.engine.xml.type.AtomicType;
import com.intel.cedar.engine.xml.type.BuiltInAtomicType;
import com.intel.cedar.engine.xml.type.SchemaType;

/**
 * NodeTest is an interface that enables a test of whether a node has a
 * particular name and kind. A NodeKindTest matches the node kind only.
 * 
 * @author Michael H. Kay
 */

public class NodeKindTest extends NodeTest {

    public static final NodeKindTest DOCUMENT = new NodeKindTest(Node.DOCUMENT);
    public static final NodeKindTest ELEMENT = new NodeKindTest(Node.ELEMENT);
    public static final NodeKindTest ATTRIBUTE = new NodeKindTest(
            Node.ATTRIBUTE);
    public static final NodeKindTest TEXT = new NodeKindTest(Node.TEXT);
    public static final NodeKindTest COMMENT = new NodeKindTest(Node.COMMENT);
    public static final NodeKindTest PROCESSING_INSTRUCTION = new NodeKindTest(
            Node.PROCESSING_INSTRUCTION);
    public static final NodeKindTest NAMESPACE = new NodeKindTest(
            Node.NAMESPACE);

    private int kind;

    private NodeKindTest(int nodeKind) {
        kind = nodeKind;
    }

    /**
     * Make a test for a given kind of node
     */

    public static NodeTest makeNodeKindTest(int kind) {
        switch (kind) {
        case Node.DOCUMENT:
            return DOCUMENT;
        case Node.ELEMENT:
            return ELEMENT;
        case Node.ATTRIBUTE:
            return ATTRIBUTE;
        case Node.COMMENT:
            return COMMENT;
        case Node.TEXT:
            return TEXT;
        case Node.PROCESSING_INSTRUCTION:
            return PROCESSING_INSTRUCTION;
        case Node.NAMESPACE:
            return NAMESPACE;
        case Node.NODE:
            return AnyNodeTest.getInstance();
        default:
            throw new IllegalArgumentException(
                    "Unknown node kind in NodeKindTest");
        }
    }

    /**
     * Test whether this node test is satisfied by a given node
     * 
     * @param nodeKind
     *            The type of node to be matched
     * @param fingerprint
     *            identifies the expanded name of the node to be matched
     */

    public boolean matches(int nodeKind, int fingerprint, int annotation) {
        return (kind == nodeKind);
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
        return node.getNodeKind() == kind;
    }

    /**
     * Determine the default priority of this node test when used on its own as
     * a Pattern
     */

    public final double getDefaultPriority() {
        return -0.5;
    }

    /**
     * Determine the types of nodes to which this pattern applies. Used for
     * optimisation.
     * 
     * @return the type of node matched by this pattern. e.g. Type.ELEMENT or
     *         Type.TEXT
     */

    public int getPrimitiveType() {
        return kind;
    }

    /**
     * Get a mask indicating which kinds of nodes this NodeTest can match. This
     * is a combination of bits: 1<<Node.ELEMENT for element nodes, 1<<Node.TEXT
     * for text nodes, and so on.
     */

    public int getNodeKindMask() {
        return 1 << kind;
    }

    public int getNodeKind() {
        return kind;
    }

    /**
     * Get the content type allowed by this NodeTest (that is, the type of
     * content allowed). Return AnyType if there are no restrictions.
     */

    public SchemaType getContentType() {
        switch (kind) {
        case Node.DOCUMENT:
            return AnyType.getInstance();
        case Node.ELEMENT:
            return AnyType.getInstance();
        case Node.ATTRIBUTE:
            return AnySimpleType.getInstance();
        case Node.COMMENT:
            return BuiltInAtomicType.STRING;
        case Node.TEXT:
            return BuiltInAtomicType.UNTYPED_ATOMIC;
        case Node.PROCESSING_INSTRUCTION:
            return BuiltInAtomicType.STRING;
        case Node.NAMESPACE:
            return BuiltInAtomicType.STRING;
        default:
            throw new AssertionError("Unknown node kind");
        }
    }

    /**
     * Get the content type allowed by this NodeTest (that is, the type
     * annotation). Return AnyType if there are no restrictions. The default
     * implementation returns AnyType.
     */

    public AtomicType getAtomizedItemType() {
        switch (kind) {
        case Node.DOCUMENT:
            return BuiltInAtomicType.UNTYPED_ATOMIC;
        case Node.ELEMENT:
            return BuiltInAtomicType.ANY_ATOMIC;
        case Node.ATTRIBUTE:
            return BuiltInAtomicType.ANY_ATOMIC;
        case Node.COMMENT:
            return BuiltInAtomicType.STRING;
        case Node.TEXT:
            return BuiltInAtomicType.UNTYPED_ATOMIC;
        case Node.PROCESSING_INSTRUCTION:
            return BuiltInAtomicType.STRING;
        case Node.NAMESPACE:
            return BuiltInAtomicType.STRING;
        default:
            throw new AssertionError("Unknown node kind");
        }
    }

    public String toString() {
        return getKindString(kind);
    }

    /**
     * Return the node test string for XPath
     */
    public String toString(int axis) {
        return toString();
    }

    public static String getKindString(int kind) {
        switch (kind) {
        case Node.DOCUMENT:
            return ("document-node()");
        case Node.ELEMENT:
            return ("element()");
        case Node.ATTRIBUTE:
            return ("attribute()");
        case Node.COMMENT:
            return ("comment()");
        case Node.TEXT:
            return ("text()");
        case Node.PROCESSING_INSTRUCTION:
            return ("processing-instruction()");
        case Node.NAMESPACE:
            return ("namespace()");
        default:
            return ("** error **");
        }
    }

    /**
     * Get the name of a node kind
     * 
     * @param kind
     *            the node kind, for example Node.ELEMENT or Node.ATTRIBUTE
     * @return the name of the node kind, for example "element" or "attribute"
     */

    public static String nodeKindName(int kind) {
        switch (kind) {
        case Node.DOCUMENT:
            return ("document");
        case Node.ELEMENT:
            return ("element");
        case Node.ATTRIBUTE:
            return ("attribute");
        case Node.COMMENT:
            return ("comment");
        case Node.TEXT:
            return ("text");
        case Node.PROCESSING_INSTRUCTION:
            return ("processing-instruction");
        case Node.NAMESPACE:
            return ("namespace");
        default:
            return ("** error **");
        }
    }

    /**
     * Returns a hash code value for the object.
     */

    public int hashCode() {
        return kind;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     */
    public boolean equals(Object other) {
        return other instanceof NodeKindTest
                && ((NodeKindTest) other).kind == kind;
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
