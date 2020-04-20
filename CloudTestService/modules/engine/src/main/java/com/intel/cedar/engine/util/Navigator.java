package com.intel.cedar.engine.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.intel.cedar.engine.xml.StandardNames;
import com.intel.cedar.engine.xml.iterator.Axis;
import com.intel.cedar.engine.xml.iterator.AxisIterator;
import com.intel.cedar.engine.xml.iterator.AxisIteratorImpl;
import com.intel.cedar.engine.xml.iterator.EmptyIterator;
import com.intel.cedar.engine.xml.iterator.NodeTest;
import com.intel.cedar.engine.xml.iterator.ReverseNodeArrayIterator;
import com.intel.cedar.engine.xml.iterator.ReversibleIterator;
import com.intel.cedar.engine.xml.iterator.SequenceIterator;
import com.intel.cedar.engine.xml.iterator.SingleNodeIterator;
import com.intel.cedar.engine.xml.model.Node;

/**
 * The Navigator class provides helper classes for navigating a tree,
 * irrespective of its implementation
 * 
 * @author Michael H. Kay
 */

public final class Navigator {

    // Class is never instantiated
    private Navigator() {
    }

    /**
     * Get the string value of an attribute of a given element, given the URI
     * and local part of the attribute name.
     * 
     * @param element
     *            the element on which the required attribute appears
     * @param uri
     *            The namespace URI of the attribute name. The null URI is
     *            represented as an empty string.
     * @param localName
     *            The local part of the attribute name.
     * @return the attribute value, or null if the attribute is not present
     * @since 9.0
     */

    public static String getAttributeValue(Node element, String uri,
            String localName) {
        int fingerprint = element.getNamePool().allocate("", uri, localName);
        return element.getAttributeValue(fingerprint);
    }

    /**
     * Helper method to get the base URI of an element or processing instruction
     * node
     * 
     * @param node
     *            the node whose base URI is required
     * @return the base URI of the node
     * @since 8.7
     */

    public static String getBaseURI(Node node) {
        String xmlBase = node.getAttributeValue(StandardNames.XML_BASE);
        if (xmlBase != null) {
            URI baseURI;
            try {
                baseURI = new URI(xmlBase);
                if (!baseURI.isAbsolute()) {
                    Node parent = node.getParentNode();
                    if (parent == null) {
                        // We have a parentless element with a relative xml:base
                        // attribute.
                        // See for example test XQTS fn-base-uri-10 and
                        // base-uri-27
                        URI base = new URI(node.getSystemId());
                        URI resolved = (xmlBase.length() == 0 ? base : base
                                .resolve(baseURI));
                        return resolved.toString();
                    }
                    String startSystemId = node.getSystemId();
                    String parentSystemId = parent.getSystemId();
                    URI base = new URI(
                            startSystemId.equals(parentSystemId) ? parent
                                    .getBaseURI() : startSystemId);
                    baseURI = (xmlBase.length() == 0 ? base : base
                            .resolve(baseURI));
                }
            } catch (URISyntaxException e) {
                // xml:base is an invalid URI. Just return it as is: the
                // operation that needs the base URI
                // will probably fail as a result. \
                return xmlBase;
            }
            return baseURI.toString();
        }
        String startSystemId = node.getSystemId();
        Node parent = node.getParentNode();
        if (parent == null) {
            return startSystemId;
        }
        String parentSystemId = parent.getSystemId();
        if (startSystemId.equals(parentSystemId)) {
            return parent.getBaseURI();
        } else {
            return startSystemId;
        }
    }

    /**
     * Create an iterator over a singleton node, if it exists and matches a
     * nodetest; otherwise return an empty iterator
     * 
     * @param node
     *            the singleton node, or null if the node does not exist
     * @param nodeTest
     *            the test to be applied
     * @return an iterator over the node if it exists and matches the test.
     */

    public static AxisIterator filteredSingleton(Node node, NodeTest nodeTest) {
        if (node != null && nodeTest.matches(node)) {
            return SingleNodeIterator.makeIterator(node);
        } else {
            return EmptyIterator.getInstance();
        }
    }

    /**
     * AxisFilter is an iterator that applies a NodeTest filter to the nodes
     * returned by an underlying AxisIterator.
     */

    public static class AxisFilter extends AxisIteratorImpl {
        private AxisIterator base;
        private NodeTest nodeTest;

        // private int last = -1;

        /**
         * S Construct a AxisFilter
         * 
         * @param base
         *            the underlying iterator that returns all the nodes on a
         *            required axis. This must not be an atomizing iterator!
         * @param test
         *            a NodeTest that is applied to each node returned by the
         *            underlying AxisIterator; only those nodes that pass the
         *            NodeTest are returned by the AxisFilter
         */

        public AxisFilter(AxisIterator base, NodeTest test) {
            this.base = base;
            nodeTest = test;
            position = 0;
        }

        public Node next() {
            while (true) {
                current = (Node) base.next();
                if (current == null) {
                    position = -1;
                    return null;
                }
                if (nodeTest.matches(current)) {
                    position++;
                    return current;
                }
            }
        }

        public SequenceIterator getAnother() {
            return new AxisFilter((AxisIterator) base.getAnother(), nodeTest);
        }
    }

    /**
     * BaseEnumeration is an abstract implementation of an AxisIterator, it
     * simplifies the implementation of the underlying AxisIterator by requiring
     * it to provide only two methods: advance(), and getAnother().
     * <p/>
     * NOTA BENE: BaseEnumeration does not maintain the value of the position
     * variable. It must therefore either (a) be wrapped in an AxisFilter, which
     * does maintain position, or (b) be subclassed by a class that maintains
     * position itself.
     */

    public static abstract class BaseIterator extends AxisIteratorImpl {

        public final Node next() {
            advance();
            return current;
        }

        /**
         * The advance() method must be provided in each concrete
         * implementation. It must leave the variable current set to the next
         * node to be returned in the iteration, or to null if there are no more
         * nodes to be returned.
         */

        public abstract void advance();

        public abstract SequenceIterator getAnother();
    }

    /**
     * General-purpose implementation of the ancestor and ancestor-or-self axes
     */

    public static final class AncestorIterator extends BaseIterator {

        private boolean includeSelf;
        private boolean atStart;
        private Node start;

        /**
         * Create an iterator over the ancestor or ancestor-or-self axis
         * 
         * @param start
         *            the initial context node
         * @param includeSelf
         *            true if the "self" node is to be included
         */

        public AncestorIterator(Node start, boolean includeSelf) {
            this.start = start;
            this.includeSelf = includeSelf;
            current = start;
            atStart = true;
        }

        public void advance() {
            if (atStart) {
                atStart = false;
                if (includeSelf) {
                    return;
                }
            }
            current = (current == null ? null : current.getParentNode());
        }

        public SequenceIterator getAnother() {
            return new AncestorIterator(start, includeSelf);
        }

    } // end of class AncestorIterator

    /**
     * General-purpose implementation of the descendant and descendant-or-self
     * axes, in terms of the child axis. But it also has the option to return
     * the descendants in reverse document order; this is used when evaluating
     * the preceding axis. Note that the includeSelf option should not be used
     * when scanning in reverse order, as the self node will always be returned
     * first.
     */

    public static final class DescendantIterator extends BaseIterator {

        private AxisIterator children = null;
        private AxisIterator descendants = null;
        private Node start;
        private boolean includeSelf;
        private boolean forwards;
        private boolean atEnd = false;

        /**
         * Create an iterator over the descendant or descendant-or-self axis
         * 
         * @param start
         *            the initial context node
         * @param includeSelf
         *            true if the "self" node is to be included
         * @param forwards
         *            true for a forwards iteration, false for reverse order
         */

        public DescendantIterator(Node start, boolean includeSelf,
                boolean forwards) {
            this.start = start;
            this.includeSelf = includeSelf;
            this.forwards = forwards;
        }

        public void advance() {
            if (descendants != null) {
                Node nextd = (Node) descendants.next();
                if (nextd != null) {
                    current = nextd;
                    return;
                } else {
                    descendants = null;
                }
            }
            if (children != null) {
                Node n = (Node) children.next();
                if (n != null) {
                    if (n.hasChildNodes()) {
                        if (forwards) {
                            descendants = new DescendantIterator(n, false,
                                    forwards);
                            current = n;
                        } else {
                            descendants = new DescendantIterator(n, true,
                                    forwards);
                            advance();
                        }
                    } else {
                        current = n;
                    }
                } else {
                    if (forwards || !includeSelf) {
                        current = null;
                    } else {
                        atEnd = true;
                        children = null;
                        current = start;
                    }
                }
            } else if (atEnd) {
                // we're just finishing a backwards scan
                current = null;
            } else {
                // we're just starting...
                if (start.hasChildNodes()) {
                    // children = new NodeWrapper.ChildIterator(start, true,
                    // forwards);
                    children = start.iterateAxis(Axis.CHILD);
                    if (!forwards) {
                        if (children instanceof ReversibleIterator) {
                            children = (AxisIterator) ((ReversibleIterator) children)
                                    .getReverseIterator();
                        } else {
                            try {
                                List list = new ArrayList(20);
                                SequenceIterator forwards = start
                                        .iterateAxis(Axis.CHILD);
                                while (true) {
                                    Node n = forwards.next();
                                    if (n == null) {
                                        break;
                                    }
                                    list.add(n);
                                }
                                Node[] nodes = new Node[list.size()];
                                nodes = (Node[]) list.toArray(nodes);
                                children = new ReverseNodeArrayIterator(nodes,
                                        0, nodes.length);
                            } catch (Exception e) {
                                throw new AssertionError(
                                        "Internal error in Navigator#descendantEnumeration: "
                                                + e.getMessage());
                                // shouldn't happen.
                            }
                        }
                    }
                } else {
                    children = EmptyIterator.getInstance();
                }
                if (forwards && includeSelf) {
                    current = start;
                } else {
                    advance();
                }
            }
        }

        public SequenceIterator getAnother() {
            return new DescendantIterator(start, includeSelf, forwards);
        }

    } // end of class DescendantIterator

    /**
     * General purpose implementation of the following axis, in terms of the
     * ancestor, child, and following-sibling axes
     */

    public static final class FollowingIterator extends BaseIterator {
        private Node start;
        private AxisIterator ancestorEnum = null;
        private AxisIterator siblingEnum = null;
        private AxisIterator descendEnum = null;

        /**
         * Create an iterator over the "following" axis
         * 
         * @param start
         *            the initial context node
         */

        public FollowingIterator(Node start) {
            this.start = start;
            ancestorEnum = new AncestorIterator(start, false);
            switch (start.getNodeKind()) {
            case Node.ELEMENT:
            case Node.TEXT:
            case Node.COMMENT:
            case Node.PROCESSING_INSTRUCTION:
                // gets following siblings
                siblingEnum = start.iterateAxis(Axis.FOLLOWING_SIBLING);
                break;
            case Node.ATTRIBUTE:
            case Node.NAMESPACE:
                // gets children of the attribute's parent node
                Node parent = start.getParentNode();
                if (parent == null) {
                    siblingEnum = EmptyIterator.getInstance();
                } else {
                    siblingEnum = parent.iterateAxis(Axis.CHILD);
                }
                break;
            default:
                siblingEnum = EmptyIterator.getInstance();
            }
            // advance();
        }

        public void advance() {
            if (descendEnum != null) {
                Node nextd = (Node) descendEnum.next();
                if (nextd != null) {
                    current = nextd;
                    return;
                } else {
                    descendEnum = null;
                }
            }
            if (siblingEnum != null) {
                Node nexts = (Node) siblingEnum.next();
                if (nexts != null) {
                    current = nexts;
                    Node n = current;
                    if (n.hasChildNodes()) {
                        descendEnum = new DescendantIterator(n, false, true);
                    } else {
                        descendEnum = null;
                    }
                    return;
                } else {
                    descendEnum = null;
                    siblingEnum = null;
                }
            }
            Node nexta = (Node) ancestorEnum.next();
            if (nexta != null) {
                current = nexta;
                Node n = current;
                if (n.getNodeKind() == Node.DOCUMENT) {
                    siblingEnum = EmptyIterator.getInstance();
                } else {
                    siblingEnum = n.iterateAxis(Axis.FOLLOWING_SIBLING);
                }
                advance();
            } else {
                current = null;
            }
        }

        public SequenceIterator getAnother() {
            return new FollowingIterator(start);
        }

    } // end of class FollowingIterator

    /**
     * Helper method to iterate over the preceding axis, or Saxon's internal
     * preceding-or-ancestor axis, by making use of the ancestor, descendant,
     * and preceding-sibling axes.
     */

    public static final class PrecedingIterator extends BaseIterator {

        private Node start;
        private AxisIterator ancestorEnum = null;
        private AxisIterator siblingEnum = null;
        private AxisIterator descendEnum = null;
        private boolean includeAncestors;

        /**
         * Create an iterator for the preceding or "preceding-or-ancestor" axis
         * 
         * @param start
         *            the initial context node
         * @param includeAncestors
         *            true if ancestors of the initial context node are to be
         *            included in the result
         */

        public PrecedingIterator(Node start, boolean includeAncestors) {
            this.start = start;
            this.includeAncestors = includeAncestors;
            ancestorEnum = new AncestorIterator(start, false);
            switch (start.getNodeKind()) {
            case Node.ELEMENT:
            case Node.TEXT:
            case Node.COMMENT:
            case Node.PROCESSING_INSTRUCTION:
                // get preceding-sibling Iterator
                siblingEnum = start.iterateAxis(Axis.PRECEDING_SIBLING);
                break;
            default:
                siblingEnum = EmptyIterator.getInstance();
            }
        }

        public void advance() {
            if (descendEnum != null) {
                Node nextd = (Node) descendEnum.next();
                if (nextd != null) {
                    current = nextd;
                    return;
                } else {
                    descendEnum = null;
                }
            }
            if (siblingEnum != null) {
                Node nexts = (Node) siblingEnum.next();
                if (nexts != null) {
                    if (nexts.hasChildNodes()) {
                        descendEnum = new DescendantIterator(nexts, true, false);
                        advance();
                    } else {
                        descendEnum = null;
                        current = nexts;
                    }
                    return;
                } else {
                    descendEnum = null;
                    siblingEnum = null;
                }
            }
            Node nexta = (Node) ancestorEnum.next();
            if (nexta != null) {
                current = nexta;
                Node n = current;
                if (n.getNodeKind() == Node.DOCUMENT) {
                    siblingEnum = EmptyIterator.getInstance();
                } else {
                    siblingEnum = n.iterateAxis(Axis.PRECEDING_SIBLING);
                }
                if (!includeAncestors) {
                    advance();
                }
            } else {
                current = null;
            }
        }

        public SequenceIterator getAnother() {
            return new PrecedingIterator(start, includeAncestors);
        }

    } // end of class PrecedingIterator

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
