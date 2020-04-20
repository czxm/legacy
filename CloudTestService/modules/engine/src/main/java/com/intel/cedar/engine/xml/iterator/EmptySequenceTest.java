package com.intel.cedar.engine.xml.iterator;

import com.intel.cedar.engine.xml.model.Node;
import com.intel.cedar.engine.xml.type.ItemType;
import com.intel.cedar.engine.xml.type.Type;

/**
 * NodeTest is an interface that enables a test of whether a node has a
 * particular name and type. An EmptySequenceTest matches no nodes or atomic
 * values: it corresponds to the type empty-sequence().
 * 
 * @author Michael H. Kay
 */

public final class EmptySequenceTest extends NodeTest {

    private static EmptySequenceTest THE_INSTANCE = new EmptySequenceTest();

    /**
     * Get a NoNodeTest instance
     */

    public static EmptySequenceTest getInstance() {
        return THE_INSTANCE;
    }

    /**
     * Private constructor
     */

    private EmptySequenceTest() {
    }

    public final int getPrimitiveType() {
        return Type.EMPTY;
    }

    /**
     * Get the primitive item type corresponding to this item type. For item(),
     * this is Type.ITEM. For node(), it is Type.NODE. For specific node kinds,
     * it is the value representing the node kind, for example Type.ELEMENT. For
     * anyAtomicValue it is Type.ATOMIC_VALUE. For numeric it is Type.NUMBER.
     * For other atomic types it is the primitive type as defined in XML Schema,
     * except that INTEGER is considered to be a primitive type.
     */

    public ItemType getPrimitiveItemType() {
        return this;
    }

    /**
     * Test whether this node test is satisfied by a given node
     * 
     * @param nodeType
     *            The type of node to be matched
     * @param fingerprint
     *            identifies the expanded name of the node to be matched
     */

    public boolean matches(int nodeType, int fingerprint, int annotation) {
        return false;
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
        return false;
    }

    /**
     * Determine the default priority of this node test when used on its own as
     * a Pattern
     */

    public final double getDefaultPriority() {
        return -0.5;
    }

    /**
     * Get a mask indicating which kinds of nodes this NodeTest can match. This
     * is a combination of bits: 1<<Type.ELEMENT for element nodes, 1<<Type.TEXT
     * for text nodes, and so on.
     */

    public int getNodeKindMask() {
        return 0;
    }

    public String toString() {
        return "empty-sequence()";
    }

    /**
     * Return the node test string for XPath
     */
    public String toString(int axis) {
        return toString();
    }

    /**
     * Returns a hash code value for the object.
     */

    public int hashCode() {
        return "NoNodeTest".hashCode();
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
