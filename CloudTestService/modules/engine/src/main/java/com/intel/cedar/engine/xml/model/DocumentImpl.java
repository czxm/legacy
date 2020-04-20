package com.intel.cedar.engine.xml.model;

import java.util.ArrayList;
import java.util.HashMap;

import com.intel.cedar.engine.xml.Configuration;
import com.intel.cedar.engine.xml.NamePool;
import com.intel.cedar.engine.xml.XMLException;
import com.intel.cedar.engine.xml.document.IDOMModel;
import com.intel.cedar.engine.xml.iterator.AxisIterator;
import com.intel.cedar.engine.xml.iterator.NodeListIterator;
import com.intel.cedar.engine.xml.util.Name11Checker;
import com.intel.cedar.engine.xml.util.NameChecker;
import com.intel.cedar.engine.xml.util.Whitespace;

/**
 * A node in the XML parse tree representing the Document itself (or
 * equivalently, the root node of the Document).
 * <P>
 * 
 * @author Michael H. Kay
 */

public final class DocumentImpl extends ParentNodeImpl implements Document {

    // private static int nextDocumentNumber = 0;

    private ElementImpl documentElement;

    private HashMap idTable = null;
    private String baseURI;
    private HashMap entityTable = null;
    private HashMap elementList = null;
    private Configuration config;
    private String systemId = null;
    private int nameCode = -1;
    private IDOMModel model = null;

    public DocumentImpl() {
        parent = null;
        ownerDocument = this;
    }

    /**
     * Set the Configuration that contains this document
     */

    public void setConfiguration(Configuration config) {
        this.config = config;
    }

    /**
     * Get the configuration previously set using setConfiguration
     */

    public Configuration getConfiguration() {
        return config;
    }

    /**
     * other nodes will be referring to this one to get the owning model
     */
    public IDOMModel getStructuredModel() {
        return model;
    }

    /**
     * setModel method
     * 
     * @param model
     *            IDOMModel
     */

    public void setStructuredModel(IDOMModel model) {
        this.model = model;
    }

    /**
     * Get the name pool used for the names in this document
     */

    public NamePool getNamePool() {
        return config.getNamePool();
    }

    /**
     * Set the top-level element of the document (variously called the root
     * element or the document element). Note that a DocumentImpl may represent
     * the root of a result tree fragment, in which case there is no document
     * element.
     * 
     * @param e
     *            the top-level element
     */

    public void setDocumentElement(ElementImpl e) {
        documentElement = e;
    }

    /**
     * Set the system id of this node
     */

    public void setSystemId(String uri) {
        if (uri == null) {
            uri = "";
        }
        systemId = uri;
    }

    /**
     * Get the system id of this root node
     */

    public String getSystemId() {
        return systemId;
    }

    /**
     * Set the base URI of this document node
     */

    public void setBaseURI(String uri) {
        baseURI = uri;
    }

    /**
     * Get the base URI of this root node.
     */

    public String getBaseURI() {
        if (baseURI != null) {
            return baseURI;
        }
        return getSystemId();
    }

    /**
     * Return the type of node.
     * 
     * @return Node.DOCUMENT (always)
     */

    public final int getNodeKind() {
        return Node.DOCUMENT;
    }

    /**
     * Get next sibling - always null
     * 
     * @return null
     */

    public final Node getNextSibling() {
        return null;
    }

    /**
     * Get previous sibling - always null
     * 
     * @return null
     */

    public final Node getPreviousSibling() {
        return null;
    }

    /**
     * Get the root (outermost) element.
     * 
     * @return the Element node for the outermost element of the document.
     */

    public Element getDocumentElement() {
        return documentElement;
    }

    /**
     * Get the root node
     * 
     * @return the Node representing the root of this tree
     */

    public Node getRoot() {
        return this;
    }

    /**
     * Get the root (document) node
     * 
     * @return the Document representing this document
     */

    public Document getOwnerDocument() {
        return this;
    }

    /**
     * Get a list of all elements with a given name fingerprint
     */

    AxisIterator getAllElements(int fingerprint) {
        Integer elkey = new Integer(fingerprint);
        if (elementList == null) {
            elementList = new HashMap(500);
        }
        ArrayList list = (ArrayList) elementList.get(elkey);
        if (list == null) {
            list = new ArrayList(500);
            NodeImpl next = getNextInDocument(this);
            while (next != null) {
                if (next.getNodeKind() == Node.ELEMENT
                        && next.getFingerprint() == fingerprint) {
                    list.add(next);
                }
                next = next.getNextInDocument(this);
            }
            elementList.put(elkey, list);
        }
        return new NodeListIterator(list);
    }

    /**
     * Index all the ID attributes. This is done the first time the id()
     * function is used on this document
     */

    private void indexIDs() {
        if (idTable != null)
            return; // ID's are already indexed
        idTable = new HashMap(256);
        NameChecker checker = Name11Checker.getInstance();

        NodeImpl curr = this;
        NodeImpl root = curr;
        while (curr != null) {
            if (curr.getNodeKind() == Node.ELEMENT) {
                ElementImpl e = (ElementImpl) curr;
                AttributeCollection atts = e.getAttributes();
                for (int i = 0; i < atts.getLength(); i++) {
                    if (atts.isId(i)
                            && checker.isValidNCName(Whitespace.trim(atts
                                    .getValue(i)))) {
                        // don't index any invalid IDs - these can arise when
                        // using a non-validating parser
                        registerID(e, Whitespace.trim(atts.getValue(i)));
                    }
                }
            }
            curr = curr.getNextInDocument(root);
        }
    }

    /**
     * Register a unique element ID. Fails if there is already an element with
     * that ID.
     * 
     * @param e
     *            The Element having a particular unique ID value
     * @param id
     *            The unique ID value
     */

    private void registerID(Node e, String id) {
        // the XPath spec (5.2.1) says ignore the second ID if it's not unique
        Object old = idTable.get(id);
        if (old == null) {
            idTable.put(id, e);
        }

    }

    /**
     * Get the element with a given ID.
     * 
     * @param id
     *            The unique ID of the required element, previously registered
     *            using registerID()
     * @return The Node for the given ID if one has been registered, otherwise
     *         null.
     */

    public Node selectID(String id) {
        if (idTable == null)
            indexIDs();
        return (Node) idTable.get(id);
    }

    /**
     * Set an unparsed entity URI associated with this document. For system use
     * only, while building the document.
     */

    public void setUnparsedEntity(String name, String uri, String publicId) {
        // System.err.println("setUnparsedEntity( " + name + "," + uri + ")");
        if (entityTable == null) {
            entityTable = new HashMap(10);
        }
        String[] ids = new String[2];
        ids[0] = uri;
        ids[1] = publicId;
        entityTable.put(name, ids);
    }

    /**
     * Get the unparsed entity with a given name
     * 
     * @param name
     *            the name of the entity
     * @return if the entity exists, return an array of two Strings, the first
     *         holding the system ID of the entity, the second holding the
     *         public ID if there is one, or null if not. If the entity does not
     *         exist, return null. * @return the URI of the entity if there is
     *         one, or empty string if not
     */

    public String[] getUnparsedEntity(String name) {
        if (entityTable == null) {
            return null;
        }
        return (String[]) entityTable.get(name);
    }

    public void setNameCode(int nc) {
        this.nameCode = nc;
    }

    public int getNameCode() {
        return this.nameCode;
    }

    /**
     * cloneNode method
     * 
     * @return org.w3c.dom.Node
     * @param deep
     *            boolean
     */
    public Node cloneNode(boolean deep) {
        throw new XMLException("operation not supported.");
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
// The Original Code is: all this file
//
// The Initial Developer of the Original Code is Michael H. Kay.
//
// Contributor(s):
//
