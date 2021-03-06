package com.intel.cedar.engine.xml.iterator;

import java.io.Serializable;

import com.intel.cedar.engine.util.IntHashSet;
import com.intel.cedar.engine.xml.NamePool;
import com.intel.cedar.engine.xml.model.Node;
import com.intel.cedar.engine.xml.type.AnyType;
import com.intel.cedar.engine.xml.type.AtomicType;
import com.intel.cedar.engine.xml.type.BuiltInAtomicType;
import com.intel.cedar.engine.xml.type.ItemType;
import com.intel.cedar.engine.xml.type.SchemaType;
import com.intel.cedar.engine.xml.type.TypeHierarchy;

/**
 * A NodeTest is a simple kind of pattern that enables a context-free test of
 * whether a node has a particular name. There are several kinds of node test: a
 * full name test, a prefix test, and an "any node of a given type" test, an
 * "any node of any type" test, a "no nodes" test (used, e.g. for "@comment()").
 * 
 * <p>
 * As well as being used to support XSLT pattern matching, NodeTests act as
 * predicates in axis steps, and also act as item types for type matching.
 * </p>
 * 
 * <p>
 * For use in user-written application calling
 * {@link Node#iterateAxis(byte, NodeTest)}, it is possible to write a
 * user-defined subclass of <code>NodeTest</code> that implements a single
 * method, {@link #matches(int, int, int)}
 * </p>
 * 
 * @author Michael H. Kay
 */

public abstract class NodeTest implements ItemType, Serializable {

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
        return AnyNodeTest.getInstance();
        // overridden for AnyNodeTest itself
    }

    /**
     * Determine the default priority of this node test when used on its own as
     * a Pattern
     */

    public abstract double getDefaultPriority();

    /**
     * Get the primitive item type corresponding to this item type. For item(),
     * this is Type.ITEM. For node(), it is Type.NODE. For specific node kinds,
     * it is the value representing the node kind, for example Type.ELEMENT. For
     * anyAtomicValue it is Type.ATOMIC_VALUE. For numeric it is Type.NUMBER.
     * For other atomic types it is the primitive type as defined in XML Schema,
     * except that INTEGER is considered to be a primitive type.
     */

    public ItemType getPrimitiveItemType() {
        int p = getPrimitiveType();
        if (p == Node.NODE) {
            return AnyNodeTest.getInstance();
        } else {
            return NodeKindTest.makeNodeKindTest(p);
        }
    }

    /**
     * Get the basic kind of object that this ItemType matches: for a NodeTest,
     * this is the kind of node, or Type.Node if it matches different kinds of
     * nodes.
     * 
     * @return the node kind matched by this node test
     */

    public int getPrimitiveType() {
        return Node.NODE;
    }

    /**
     * Get the name of the nodes matched by this nodetest, if it matches a
     * specific name. Return -1 if the node test matches nodes of more than one
     * name
     */

    public int getFingerprint() {
        return -1;
    }

    /**
     * Determine whether this item type is atomic (that is, whether it can ONLY
     * match atomic values)
     * 
     * @return false: this is not ANY_ATOMIC_TYPE or a subtype thereof
     */

    public boolean isAtomicType() {
        return false;
    }

    /**
     * Get the item type of the atomic values that will be produced when an item
     * of this type is atomized (assuming that atomization succeeds)
     */

    public AtomicType getAtomizedItemType() {
        // This is overridden for a ContentTypeTest
        return BuiltInAtomicType.ANY_ATOMIC;
    }

    /**
     * Test whether this node test is satisfied by a given node. This method is
     * only fully supported for a subset of NodeTests, because it doesn't
     * provide all the information needed to evaluate all node tests. In
     * particular (a) it can't be used to evaluate a node test of the form
     * element(N,T) or schema-element(E) where it is necessary to know whether
     * the node is nilled, and (b) it can't be used to evaluate a node test of
     * the form document-node(element(X)). This in practice means that it is
     * used (a) to evaluate the simple node tests found in the XPath 1.0 subset
     * used in XML Schema, and (b) to evaluate node tests where the node kind is
     * known to be an attribute.
     * 
     * @param nodeKind
     *            The kind of node to be matched
     * @param fingerprint
     *            identifies the expanded name of the node to be matched. The
     *            value should be -1 for a node with no name.
     * @param annotation
     *            The actual content type of the node
     * 
     */

    public abstract boolean matches(int nodeKind, int fingerprint,
            int annotation);

    /**
     * Test whether this node test is satisfied by a given node. This
     * alternative method is used in the case of nodes where calculating the
     * fingerprint is expensive, for example DOM or JDOM nodes. The default
     * implementation calls the method {@link #matches(int, int, int)}
     * 
     * @param node
     *            the node to be matched
     */

    public boolean matches(Node node) {
        return matches(node.getNodeKind(), node.getFingerprint(), node
                .getTypeAnnotation());
    }

    /**
     * Get a mask indicating which kinds of nodes this NodeTest can match. This
     * is a combination of bits: 1<<Node.ELEMENT for element nodes, 1<<Node.TEXT
     * for text nodes, and so on. The default implementation indicates that
     * nodes of all kinds are matched.
     */

    public int getNodeKindMask() {
        return 1 << Node.ELEMENT | 1 << Node.TEXT | 1 << Node.COMMENT
                | 1 << Node.PROCESSING_INSTRUCTION | 1 << Node.ATTRIBUTE
                | 1 << Node.NAMESPACE | 1 << Node.DOCUMENT;
    }

    /**
     * Get the content type allowed by this NodeTest (that is, the type
     * annotation of the matched nodes). Return AnyType if there are no
     * restrictions. The default implementation returns AnyType.
     */

    public SchemaType getContentType() {
        return AnyType.getInstance();
    }

    /**
     * Get the set of node names allowed by this NodeTest. This is returned as a
     * set of Integer fingerprints. A null value indicates that all names are
     * permitted (i.e. that there are no constraints on the node name. The
     * default implementation returns null.
     */

    public IntHashSet getRequiredNodeNames() {
        return null;
    }

    /**
     * Determine whether the content type (if present) is nillable
     * 
     * @return true if the content test (when present) can match nodes that are
     *         nilled
     */

    public boolean isNillable() {
        return false;
    }

    /**
     * Display the type descriptor for diagnostics
     */

    public String toString(NamePool pool) {
        return toString();
    }

    /**
     * Return the node test string for XPath
     */
    abstract public String toString(int axis);

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
