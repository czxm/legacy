package com.intel.cedar.engine.xml.model;

import java.util.ArrayList;
import java.util.List;

import com.intel.cedar.engine.util.Navigator;
import com.intel.cedar.engine.xml.XMLException;
import com.intel.cedar.engine.xml.document.IDOMModel;
import com.intel.cedar.engine.xml.iterator.AnyNodeTest;
import com.intel.cedar.engine.xml.iterator.AxisIterator;
import com.intel.cedar.engine.xml.iterator.ChildIterator;
import com.intel.cedar.engine.xml.iterator.EmptyIterator;
import com.intel.cedar.engine.xml.iterator.NodeListIterator;
import com.intel.cedar.engine.xml.iterator.NodeTest;
import com.intel.cedar.engine.xml.iterator.SingleNodeIterator;

/**
 * ParentNodeImpl is an implementation of a non-leaf node (specifically, an
 * Element node or a Document node)
 * 
 * @author Michael H. Kay
 */

public abstract class ParentNodeImpl extends NodeImpl {

    private Object children = null; // null for no children
    // a Node for a single child
    // a List for >1 child

    private boolean fChildEditable = true;

    public ParentNodeImpl() {
    }

    /**
     * ParentNodeImpl constructor
     * 
     * @param that
     *            ParentNodeImpl
     */
    protected ParentNodeImpl(ParentNodeImpl that) {
        super(that);
    }

    public ParentNodeImpl(DocumentImpl ownerDocument) {
        super(ownerDocument);
    }

    /**
     * Determine if the node has any children.
     */

    public final boolean hasChildNodes() {
        return (children != null);
    }

    /**
     * Get an enumeration of the children of this node
     * 
     * @param test
     *            A NodeTest to be satisfied by the child nodes, or null if all
     *            child node are to be returned
     * @return an iterator over the children of this node
     */

    protected final AxisIterator enumerateChildren(NodeTest test) {
        if (children == null) {
            return EmptyIterator.getInstance();
        } else if (children instanceof NodeImpl) {
            NodeImpl child = (NodeImpl) children;
            if (test == null || test instanceof AnyNodeTest) {
                return SingleNodeIterator.makeIterator(child);
            } else {
                return Navigator.filteredSingleton(child, test);
            }
        } else {
            if (test == null || test instanceof AnyNodeTest) {
                return new NodeListIterator((List) children);
            } else {
                return new ChildIterator(this, test);
            }
        }
    }

    /**
     * Get the first child node of the element
     * 
     * @return the first child node of the required type, or null if there are
     *         no children
     */

    public final Node getFirstChild() {
        if (children == null)
            return null;
        if (children instanceof NodeImpl)
            return (NodeImpl) children;

        ArrayList<NodeImpl> n = (ArrayList<NodeImpl>) children;
        if (n.size() <= 0)
            return null;

        return n.get(0);
    }

    /**
     * Get the last child node of the element
     * 
     * @return the last child of the element, or null if there are no children
     */

    public final Node getLastChild() {
        if (children == null)
            return null;
        if (children instanceof NodeImpl)
            return (NodeImpl) children;
        ArrayList<NodeImpl> n = (ArrayList<NodeImpl>) children;

        // consider the list may be empty
        int size = n.size();
        if (size <= 0)
            return null;

        return n.get(size - 1);
    }

    /**
     * Get the nth child node of the element (numbering from 0)
     * 
     * @param n
     *            identifies the required child
     * @return the last child of the element, or null if there is no n'th child
     */

    protected final NodeImpl getNthChild(int n) {
        if (children == null)
            return null;
        if (children instanceof NodeImpl) {
            return (n == 0 ? (NodeImpl) children : null);
        }
        ArrayList<NodeImpl> nodes = (ArrayList<NodeImpl>) children;
        if (n < 0 || n >= nodes.size())
            return null;
        return nodes.get(n);
    }

    /**
     * Return the string-value of the node, that is, the concatenation of the
     * character content of all descendent elements and text nodes.
     * 
     * @return the accumulated character content of the element, including
     *         descendant elements.
     */

    public String getStringValue() {
        return getStringValueCS().toString();
    }

    public CharSequence getStringValueCS() {
        StringBuffer sb = null;

        NodeImpl next = (NodeImpl) getFirstChild();
        while (next != null) {
            if (next instanceof TextImpl) {
                if (sb == null) {
                    sb = new StringBuffer(1024);
                }
                sb.append(next.getStringValueCS());
            }
            next = next.getNextInDocument(this);
        }
        if (sb == null)
            return "";
        return sb;
    }

    /**
     * Add a child node to this node. For system use only. Note: normalizing
     * adjacent text nodes is the responsibility of the caller.
     * 
     * @param node
     *            the node to be added as a child of this node
     * @param index
     *            the position where the child is to be added
     */

    public void buildChild(NodeImpl node) {
        NodeImpl lastChild = (NodeImpl) getLastChild();

        if (children == null) {
            children = node;
        } else if (children instanceof NodeImpl) {
            ArrayList<NodeImpl> c = new ArrayList<NodeImpl>(10);
            c.add((NodeImpl) children);
            c.add(node);
            children = c;
        } else {
            ((ArrayList<NodeImpl>) children).add(node);
        }

        node.parent = this;
        node.nextSibling = null;
        node.previousSibling = lastChild;
        if (null != node.previousSibling)
            node.previousSibling.nextSibling = node;
    }

    /**
     * Compact the space used by this node
     * 
     * @param size
     *            the number of actual children
     */

    public void compact() {

    }

    /**
     * Used to know read-only state of children.
     * 
     * @return boolean Whether children of the element can be appended or
     *         removed.
     */
    public boolean isChildEditable() {
        return fChildEditable;
    }

    /**
     * Set whether the children is read-only.
     * 
     */
    public void setChildEditable(boolean editable) {
        if (fChildEditable == editable) {
            return;
        }

        fChildEditable = editable;
    }

    /**
     * Add a child node to this node. For system use only. Note: normalizing
     * adjacent text nodes is the responsibility of the caller.
     * 
     * @param newChild
     *            the node to be added as a child of this node
     */

    public Node appendChild(Node newChild) throws XMLException {
        return insertBefore(newChild, null);
    }

    /**
     * insertBefore method
     * 
     * @return org.w3c.dom.Node
     * @param newChild
     *            org.w3c.dom.Node
     * @param refChild
     *            org.w3c.dom.Node
     */
    public Node insertBefore(Node newChild, Node refChild) throws XMLException {
        if (newChild == null)
            return null; // nothing to do
        if (refChild != null && refChild.getParentNode() != this) {
            throw new XMLException("reference child is not found.");
        }
        if (!isChildEditable()) {
            throw new XMLException("children list is read-only.");
        }
        if (newChild == refChild)
            return newChild; // nothing to do

        int index = -1;
        if (refChild != null) {
            index = findChild(refChild);
            if (index < 0)
                throw new XMLException("reference child is not found.");
        }

        NodeImpl child = (NodeImpl) newChild;
        NodeImpl next = (NodeImpl) refChild;
        NodeImpl prev = null;
        Node oldParent = child.getParentNode();
        if (oldParent != null) {
            // this child may not be added to the parent
            try {
                oldParent.removeChild(child);
            } catch (XMLException e) {
                // not found, ignore
            }
        }

        if (next == null) {
            prev = (NodeImpl) this.getLastChild();
        } else {
            prev = (NodeImpl) next.getPreviousSibling();
            next.setPreviousSibling(child);
        }

        if (prev != null)
            prev.setNextSibling(child);

        child.setPreviousSibling(prev);
        child.setNextSibling(next);
        child.setParentNode(this);

        // make sure having the same owner document
        if (child.getOwnerDocument() == null) {
            if (getNodeKind() == Node.DOCUMENT) {
                child.setOwnerDocument((Document) this);
            } else {
                child.setOwnerDocument(getOwnerDocument());
            }
        }

        insert(index, child);

        notifyChildReplaced(child, null);

        return child;
    }

    /**
     * insertBefore method
     * 
     * @return org.w3c.dom.Node
     * @param newChild
     *            org.w3c.dom.Node
     * @param index
     *            the position to insert
     */
    public Node insertAt(Node newChild, int index) throws XMLException {
        if (newChild == null)
            return null; // nothing to do
        if (!isChildEditable()) {
            throw new XMLException("children list is read-only.");
        }

        NodeImpl child = (NodeImpl) newChild;
        NodeImpl next = (NodeImpl) getNthChild(index);

        NodeImpl prev = null;
        Node oldParent = child.getParentNode();
        if (oldParent != null) {
            // this child may not be added to the parent
            try {
                oldParent.removeChild(child);
            } catch (XMLException e) {
                // not found, ignore
            }
        }

        if (next == null) {
            prev = (NodeImpl) this.getLastChild();
        } else {
            prev = (NodeImpl) next.getPreviousSibling();
            next.setPreviousSibling(child);
        }

        if (prev != null)
            prev.setNextSibling(child);

        child.setPreviousSibling(prev);
        child.setNextSibling(next);
        child.setParentNode(this);

        // make sure having the same owner document
        if (child.getOwnerDocument() == null) {
            if (getNodeKind() == Node.DOCUMENT) {
                child.setOwnerDocument((Document) this);
            } else {
                child.setOwnerDocument(getOwnerDocument());
            }
        }

        insert(index, child);

        notifyChildReplaced(child, null);

        return child;
    }

    /**
     * Removes the child node indicated by oldChild from the list of children,
     * and returns it.
     * 
     * @param oldChild
     *            the node to be removed
     */
    public Node removeChild(Node oldChild) throws XMLException {
        int index = internalRemoveChild(oldChild);
        if (index < 0)
            return null;

        return oldChild;
    }

    public int internalRemoveChild(Node oldChild) throws XMLException {
        if (oldChild == null)
            return -1;
        if (oldChild.getParentNode() != this) {
            throw new XMLException("child node is not found.");
        }

        if (!isChildEditable()) {
            throw new XMLException("children list is read-only.");
        }

        int index = findChild(oldChild);
        if (index < 0)
            return -1;

        NodeImpl child = (NodeImpl) oldChild;
        NodeImpl prev = (NodeImpl) child.getPreviousSibling();
        NodeImpl next = (NodeImpl) child.getNextSibling();

        if (prev != null)
            prev.setNextSibling(next);

        if (next != null)
            next.setPreviousSibling(prev);

        child.setPreviousSibling(null);
        child.setNextSibling(null);
        child.setParentNode(null);

        remove(index);

        notifyChildReplaced(null, child);

        return index;
    }

    /**
     * Replaces the child node oldChild with newChild in the list of children,
     * and returns the oldChild node.
     * 
     */
    public Node replaceChild(Node newChild, Node oldChild) throws XMLException {
        if (!isChildEditable()) {
            throw new XMLException("children list is read-only.");
        }

        if (oldChild == null)
            return newChild;
        if (newChild != null)
            insertBefore(newChild, oldChild);
        return removeChild(oldChild);
    }

    protected void insert(int index, NodeImpl node) {
        if (children == null) {
            children = node;
        } else if (children instanceof NodeImpl) {
            ArrayList<NodeImpl> c = new ArrayList<NodeImpl>(10);
            c.add((NodeImpl) children);
            if (index < 0)
                c.add(node);
            else
                c.add(index, node);
            children = c;
        } else {
            ArrayList<NodeImpl> c = (ArrayList<NodeImpl>) children;
            if (index < 0)
                c.add(node);
            else
                c.add(index, node);
        }
    }

    protected boolean remove(int index) {
        if (children == null) {
            return true;
        } else if (children instanceof NodeImpl) {
            if (index != 0)
                return false;

            children = null;
            return true;
        } else {
            ArrayList<NodeImpl> c = (ArrayList<NodeImpl>) children;
            if (index >= c.size())
                return false;
            c.remove(index);
            return true;
        }
    }

    public int findChild(Node refChild) {
        if (children == null) {
            return -1;
        } else if (children instanceof NodeImpl) {
            if (children == refChild)
                return 0;
            else
                return -1;
        } else {
            ArrayList<NodeImpl> c = (ArrayList<NodeImpl>) children;
            int size = c.size();
            for (int i = 0; i < size; i++) {
                if (c.get(i) == refChild)
                    return i;
            } // for
        }

        return -1;
    }

    /**
     * notifyChildReplaced method
     */
    protected void notifyChildReplaced(Node newChild, Node oldChild) {
        DocumentImpl document = (DocumentImpl) getOwnerDocument();
        if (document == null)
            return;

        IDOMModel model = (IDOMModel) document.getStructuredModel();
        if (model == null)
            return;

        model.childReplaced(this, newChild, oldChild);
    }

    /**
     * cloneChildNodes method
     * 
     * @param container
     *            org.w3c.dom.Node
     * @param deep
     *            boolean
     */
    protected void cloneChildNodes(Node newParent, boolean deep) {
        if (newParent == null || newParent == this)
            return;
        if (!(newParent instanceof ParentNodeImpl))
            return;

        // assume that the new parent is empty
        ParentNodeImpl container = (ParentNodeImpl) newParent;

        for (Node child = getFirstChild(); child != null; child = child
                .getNextSibling()) {
            Node cloned = child.cloneNode(deep);
            if (cloned != null)
                container.appendChild(cloned);
        }
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
