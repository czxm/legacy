package com.intel.cedar.engine.xml.iterator;

import com.intel.cedar.engine.xml.model.AttributeCollection;
import com.intel.cedar.engine.xml.model.ElementImpl;
import com.intel.cedar.engine.xml.model.Node;
import com.intel.cedar.engine.xml.model.NodeImpl;

/**
 * AttributeIterator is an Iterator of all the attribute nodes of an Element.
 */

public final class AttributeIterator extends AxisIteratorImpl implements
        LookaheadIterator {

    private ElementImpl element;
    private NodeTest nodeTest;
    private Node next;
    private int index;
    private int length;

    /**
     * Constructor
     * 
     * @param node
     *            : the element whose attributes are required. This may be any
     *            type of node, but if it is not an element the Iterator will be
     *            empty
     * @param nodeTest
     *            : condition to be applied to the names of the attributes
     *            selected
     */

    public AttributeIterator(NodeImpl node, NodeTest nodeTest) {
        this.nodeTest = nodeTest;

        if (node.getNodeKind() == Node.ELEMENT) {
            element = (ElementImpl) node;
            AttributeCollection attlist = element.getAttributes();
            index = 0;

            if (nodeTest instanceof NameTest) {
                NameTest test = (NameTest) nodeTest;
                index = attlist.getIndexByFingerprint(test.getFingerprint());

                if (index < 0) {
                    next = null;
                } else {
                    next = attlist.getAttribute(index);
                    index = 0;
                    length = 0; // force iteration to select one node only
                }

            } else {
                index = 0;
                length = attlist.getLength();
                advance();
            }
        } else { // if it's not an element, or if we're not looking for
                 // attributes,
            // then there's nothing to find
            next = null;
            index = 0;
            length = 0;
        }
    }

    /**
     * Test if there are mode nodes still to come. ("elements" is used here in
     * the sense of the Java Iterator class, not in the XML sense)
     */

    public boolean hasNext() {
        return next != null;
    }

    /**
     * Get the next node in the iteration, or null if there are no more.
     */

    public Node next() {
        if (next == null) {
            current = null;
            position = -1;
            return null;
        } else {
            current = next;
            position++;
            advance();
            return current;
        }
    }

    /**
     * Move to the next node in the Iterator.
     */

    private void advance() {
        AttributeCollection attlist = element.getAttributes();
        do {
            if (index < length) {
                next = attlist.getAttribute(index);
                index++;
            } else {
                next = null;
                return;
            }
        } while (!nodeTest.matches(next));
    }

    /**
     * Get another Iterator of the same nodes
     */

    public SequenceIterator getAnother() {
        return new AttributeIterator(element, nodeTest);
    }

    /**
     * Get properties of this iterator, as a bit-significant integer.
     * 
     * @return the properties of this iterator. This will be some combination of
     *         properties such as {@link GROUNDED}, {@link LAST_POSITION_FINDER}
     *         , and {@link LOOKAHEAD}. It is always acceptable to return the
     *         value zero, indicating that there are no known special
     *         properties. It is acceptable for the properties of the iterator
     *         to change depending on its state.
     */

    public int getProperties() {
        return LOOKAHEAD;
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
