package com.intel.cedar.engine.xml.iterator;

import com.intel.cedar.engine.xml.model.Node;
import com.intel.cedar.engine.xml.type.AnyItemType;
import com.intel.cedar.engine.xml.type.ItemType;
import com.intel.cedar.engine.xml.type.TypeHierarchy;

/**
 * NodeTest is an interface that enables a test of whether a node has a
 * particular name and Node. An AnyNodeTest matches any node.
 * 
 * @author Michael H. Kay
 */

public final class AnyNodeTest extends NodeTest {

    private static AnyNodeTest THE_INSTANCE = new AnyNodeTest();

    /**
     * Get an instance of AnyNodeTest
     */

    public static AnyNodeTest getInstance() {
        return THE_INSTANCE;
    }

    /**
     * Private constructor
     */

    private AnyNodeTest() {
    }

    /**
     * Test whether this node test is satisfied by a given node
     * 
     * @param nodeType
     *            The type of node to be matched
     * @param fingerprint
     *            identifies the expanded name of the node to be matched
     */

    public final boolean matches(int nodeType, int fingerprint, int annotation) {
        return true;
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
        return true;
    }

    public ItemType getSuperType(TypeHierarchy th) {
        return AnyItemType.getInstance();
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
     * is a combination of bits: 1<<Node.ELEMENT for element nodes, 1<<Node.TEXT
     * for text nodes, and so on.
     */

    public int getNodeKindMask() {
        return 1 << Node.ELEMENT | 1 << Node.TEXT | 1 << Node.COMMENT
                | 1 << Node.PROCESSING_INSTRUCTION | 1 << Node.ATTRIBUTE
                | 1 << Node.NAMESPACE | 1 << Node.DOCUMENT;
    }

    public String toString() {
        return "node()";
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
        return "AnyNodeTest".hashCode();
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
