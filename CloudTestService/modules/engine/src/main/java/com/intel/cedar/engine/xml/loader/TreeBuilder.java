package com.intel.cedar.engine.xml.loader;

import com.intel.cedar.engine.xml.XMLException;
import com.intel.cedar.engine.xml.model.AttributeCollectionImpl;
import com.intel.cedar.engine.xml.model.CommentImpl;
import com.intel.cedar.engine.xml.model.Document;
import com.intel.cedar.engine.xml.model.DocumentImpl;
import com.intel.cedar.engine.xml.model.ElementImpl;
import com.intel.cedar.engine.xml.model.ParentNodeImpl;
import com.intel.cedar.engine.xml.model.ProcessingInstructionImpl;
import com.intel.cedar.engine.xml.model.TextImpl;

/**
 * The Builder class is responsible for taking a stream of SAX events and
 * constructing a Document tree.
 * 
 * @author Michael H. Kay
 */

public class TreeBuilder extends Builder

{
    private ParentNodeImpl currentNode;

    private NodeFactory nodeFactory;
    private int depth = 0;
    private int pendingElement;
    private long pendingLocationId;
    private AttributeCollectionImpl attributes;
    private int[] namespaces;
    private int namespacesUsed;

    private static final int[] EMPTY_ARRAY_OF_INT = new int[0];

    /**
     * create a Builder and initialise variables
     */

    public TreeBuilder() {
        nodeFactory = new DefaultNodeFactory();
    }

    /**
     * Set the Node Factory to use. If none is specified, the Builder uses its
     * own.
     * 
     * @param factory
     *            the node factory to be used. This allows custom objects to be
     *            used to represent the elements in the tree.
     */

    public void setNodeFactory(NodeFactory factory) {
        nodeFactory = factory;
    }

    // //////////////////////////////////////////////////////////////////////////////////////
    // Implement the org.xml.sax.ContentHandler interface.
    // //////////////////////////////////////////////////////////////////////////////////////

    /**
     * Callback interface for SAX: not for application use
     */

    public void open() throws XMLException {
        // System.err.println("TreeBuilder: " + this + " Start document depth="
        // + depth);
        // failed = false;
        started = true;

        DocumentImpl doc;
        if (currentRoot == null) {
            // normal case
            doc = new DocumentImpl();
            currentRoot = doc;
        } else {
            // document node supplied by user
            if (!(currentRoot instanceof DocumentImpl)) {
                throw new XMLException(
                        "Document node supplied is of wrong kind ("
                                + currentRoot.getClass().getName() + ')');
            }
            doc = (DocumentImpl) currentRoot;
            if (doc.getFirstChild() != null) {
                throw new XMLException("Supplied document is not empty");
            }

        }

        doc.setSystemId(getSystemId());
        doc.setBaseURI(getBaseURI());
        doc.setConfiguration(config);
        currentNode = doc;
        depth = 0;

        super.open();
    }

    /**
     * Callback interface for SAX: not for application use
     */

    public void close() throws XMLException {
        // System.err.println("TreeBuilder: " + this + " End document");
        if (currentNode == null)
            return; // can be called twice on an error path
        currentNode.compact();
        currentNode = null;

        super.close();
        nodeFactory = null;

    }

    /**
     * Notify the start of an element
     */

    public void startElement(int nameCode, int typeCode, int locationId,
            int properties) throws XMLException {
        // System.err.println("TreeBuilder: " + this + " Start element depth=" +
        // depth);

        pendingElement = nameCode;
        pendingLocationId = locationId;
        // namespaces = null;
        namespacesUsed = 0;
        attributes = null;
    }

    public void namespace(int namespaceCode, int properties) {
        if (namespaces == null) {
            namespaces = new int[5];
        }
        if (namespacesUsed == namespaces.length) {
            int[] ns2 = new int[namespaces.length * 2];
            System.arraycopy(namespaces, 0, ns2, 0, namespacesUsed);
            namespaces = ns2;
        }
        namespaces[namespacesUsed++] = namespaceCode;
    }

    public void attribute(int nameCode, int typeCode, CharSequence value,
            int locationId, int properties) throws XMLException {

        // if ((properties & ReceiverOptions.DISABLE_ESCAPING) != 0) {
        // throw new
        // XSLException("Cannot disable output escaping when writing a tree");
        // }
        properties &= ~ReceiverOptions.DISABLE_ESCAPING;

        if (attributes == null) {
            attributes = new AttributeCollectionImpl(namePool);
        }

        int lineNumber = -1;
        LocationProvider locator = receiverConfiguration.getLocationProvider();
        if (locator != null)
            lineNumber = locator.getLineNumber(locationId);

        attributes.addAttribute(nameCode, typeCode, value.toString(),
                lineNumber, properties);
    }

    public void startContent() throws XMLException {
        // System.err.println("TreeBuilder: " + this + " startContent()");
        if (attributes == null) {
            attributes = new AttributeCollectionImpl(namePool);
        } else {
            attributes.compact();
        }

        int[] nslist = namespaces;
        if (nslist == null || namespacesUsed == 0) {
            nslist = EMPTY_ARRAY_OF_INT;
        }

        ElementImpl elem = nodeFactory.makeElementNode(
                (DocumentImpl) currentRoot, pendingElement, attributes, nslist,
                namespacesUsed, receiverConfiguration.getLocationProvider(),
                (int) pendingLocationId);

        // namespaces = null;
        namespacesUsed = 0;
        attributes = null;
        currentNode.buildChild(elem);
        ++depth;
        namespacesUsed = 0;

        if (currentNode instanceof Document) {
            ((DocumentImpl) currentNode).setDocumentElement(elem);
        }

        currentNode = elem;
    }

    /**
     * Notify the end of an element
     */

    public void endElement() throws XMLException {
        // System.err.println("End element depth=" + depth);
        currentNode.compact();
        depth--;
        currentNode = (ParentNodeImpl) currentNode.getParentNode();
    }

    /**
     * Notify a text node. Adjacent text nodes must have already been merged
     */

    public void characters(CharSequence chars, int locationId, int properties)
            throws XMLException {
        // System.err.println("Characters: " + chars.toString() + " depth=" +
        // depth);
        if (chars.length() > 0) {
            // we rely on adjacent chunks of text having already been merged

            int lineNumber = -1;
            LocationProvider locator = receiverConfiguration
                    .getLocationProvider();
            if (locator != null)
                lineNumber = locator.getLineNumber(locationId);

            TextImpl n = new TextImpl((DocumentImpl) currentRoot, chars
                    .toString());
            n.setLineNumber(lineNumber);
            currentNode.buildChild(n);
        }
    }

    /**
     * Notify a processing instruction
     */

    public void processingInstruction(String name, CharSequence remainder,
            int locationId, int properties) {
        int lineNumber = -1;
        LocationProvider locator = receiverConfiguration.getLocationProvider();
        if (locator != null)
            lineNumber = locator.getLineNumber(locationId);

        int nameCode = namePool.allocate("", "", name);
        ProcessingInstructionImpl pi = new ProcessingInstructionImpl(
                (DocumentImpl) currentRoot, nameCode, remainder.toString());
        pi.setLineNumber(lineNumber);
        currentNode.buildChild(pi);
    }

    /**
     * Notify a comment
     */

    public void comment(CharSequence chars, int locationId, int properties)
            throws XMLException {
        int lineNumber = -1;
        LocationProvider locator = receiverConfiguration.getLocationProvider();
        if (locator != null)
            lineNumber = locator.getLineNumber(locationId);

        CommentImpl comment = new CommentImpl((DocumentImpl) currentRoot, chars
                .toString());
        comment.setLineNumber(lineNumber);
        currentNode.buildChild(comment);
    }

    /**
     * graftElement() allows an element node to be transferred from one tree to
     * another. This is a dangerous internal interface which is used only to
     * contruct a stylesheet tree from a stylesheet using the
     * "literal result element as stylesheet" syntax. The supplied element is
     * grafted onto the current element as its only child.
     * 
     * @param element
     *            the element to be grafted in as a new child.
     */

    public void graftElement(ElementImpl element) throws XMLException {
        currentNode.buildChild(element);
    }

    /**
     * Set an unparsed entity URI for the document
     */

    public void setUnparsedEntity(String name, String uri, String publicId) {
        ((DocumentImpl) currentRoot).setUnparsedEntity(name, uri, publicId);
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
