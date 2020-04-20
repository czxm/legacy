package com.intel.cedar.engine.xml.iterator;

import com.intel.cedar.engine.xml.NamePool;
import com.intel.cedar.engine.xml.model.Node;

/**
 * A DocumentNodeTest implements the test document-node(element(~,~))
 */

// This is messy because the standard interface for a NodeTest does not allow
// any navigation from the node in question - it only tests for the node kind,
// node name, and type annotation of the node.

public class DocumentNodeTest extends NodeTest {

    private NodeTest elementTest;

    public DocumentNodeTest(NodeTest elementTest) {
        this.elementTest = elementTest;
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
        throw new UnsupportedOperationException(
                "DocumentNodeTest doesn't support this method");
    }

    /**
     * Determine whether this Pattern matches the given Node.
     * 
     * @param node
     *            The Node representing the Element or other node to be tested
     *            against the Pattern uses variables, or contains calls on
     *            functions such as document() or key().
     * @return true if the node matches the Pattern, false otherwise
     */

    public boolean matches(Node node) {
        if (node.getNodeKind() != Node.DOCUMENT) {
            return false;
        }
        AxisIterator iter = node.iterateAxis(Axis.CHILD);
        // The match is true if there is exactly one element node child, no text
        // node
        // children, and the element node matches the element test.
        boolean found = false;
        while (true) {
            Node n = (Node) iter.next();
            if (n == null) {
                return found;
            }
            int kind = n.getNodeKind();
            if (kind == Node.TEXT) {
                return false;
            } else if (kind == Node.ELEMENT) {
                if (found) {
                    return false;
                }
                if (elementTest.matches(n)) {
                    found = true;
                } else {
                    return false;
                }
            }
        }
    }

    /**
     * Determine the default priority of this node test when used on its own as
     * a Pattern
     */

    public final double getDefaultPriority() {
        return elementTest.getDefaultPriority();
    }

    /**
     * Determine the types of nodes to which this pattern applies. Used for
     * optimisation.
     * 
     * @return the type of node matched by this pattern. e.g. Node.ELEMENT or
     *         Node.TEXT
     */

    public int getPrimitiveType() {
        return Node.DOCUMENT;
    }

    /**
     * Get a mask indicating which kinds of nodes this NodeTest can match. This
     * is a combination of bits: 1<<Node.ELEMENT for element nodes, 1<<Node.TEXT
     * for text nodes, and so on.
     */

    public int getNodeKindMask() {
        return 1 << Node.DOCUMENT;
    }

    /**
     * Get the element test contained within this document test
     * 
     * @return the contained element test
     */

    public NodeTest getElementTest() {
        return elementTest;
    }

    public String toString(NamePool pool) {
        return "document-node(" + elementTest.toString(pool) + ')';
    }

    public String toString() {
        return "document-node(" + elementTest.toString() + ')';
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
        return elementTest.hashCode() ^ 12345;
    }

    public boolean equals(Object other) {
        return other instanceof DocumentNodeTest
                && ((DocumentNodeTest) other).elementTest.equals(elementTest);
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
