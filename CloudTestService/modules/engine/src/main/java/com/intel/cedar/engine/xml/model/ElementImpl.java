package com.intel.cedar.engine.xml.model;

import java.util.Iterator;

import com.intel.cedar.engine.util.Navigator;
import com.intel.cedar.engine.xml.NamePool;
import com.intel.cedar.engine.xml.NamespaceConstant;
import com.intel.cedar.engine.xml.NamespaceException;
import com.intel.cedar.engine.xml.NamespaceResolver;
import com.intel.cedar.engine.xml.StandardNames;
import com.intel.cedar.engine.xml.XMLException;
import com.intel.cedar.engine.xml.document.IDOMModel;
import com.intel.cedar.engine.xml.iterator.IntIterator;
import com.intel.cedar.engine.xml.iterator.NamespaceCodeIterator;
import com.intel.cedar.engine.xml.util.NameChecker;

/**
 * ElementImpl implements an element with no attributes or namespace
 * declarations.
 * <P>
 * This class is an implementation of Node. For elements with attributes or
 * namespace declarations, class ElementWithAttributes is used.
 * 
 * @author Michael H. Kay
 */

public class ElementImpl extends ParentNodeImpl implements Element,
        NamespaceResolver {

    protected AttributeCollectionImpl attributeList; // this excludes namespace
                                                     // attributes
    protected int[] namespaceList = null; // list of namespace codes
    // note that this namespace list includes only the namespaces actually
    // defined on
    // this element, not those inherited from outer elements.
    public static final int[] EMPTY_INT_ARRAY = new int[0];

    protected int nameCode;

    /**
     * Construct an empty ElementImpl
     */

    public ElementImpl() {
    }

    /**
     * ElementImpl constructor
     * 
     * @param that
     *            ElementImpl
     */
    protected ElementImpl(ElementImpl that) {
        super(that);

        if (that != null) {
            this.nameCode = that.nameCode;

            // clone attributes
            that.cloneAttributes(this);

            if (that.namespaceList != null)
                setNamespaceDeclarations(that.namespaceList,
                        that.namespaceList.length);

        }
    }

    public ElementImpl(DocumentImpl ownerDocument) {
        super(ownerDocument);
    }

    /**
     * Initialise a new ElementImpl with an element name
     * 
     * @param nameCode
     *            Integer representing the element name, with namespaces
     *            resolved
     * @param atts
     *            The attribute list: always null
     * @param parent
     *            The parent node
     * @param baseURI
     *            The base URI of the new element
     * @param lineNumber
     *            The line number of the element in the source document
     * @param sequenceNumber
     *            Integer identifying this element within the document
     */

    public void initialise(int nameCode, AttributeCollectionImpl atts,
            DocumentImpl ownerDocument) {
        this.nameCode = nameCode;
        this.attributeList = atts;
        this.ownerDocument = ownerDocument;

        if (attributeList != null)
            attributeList.setOwner(this);
    }

    /**
     * Get the system ID of the entity containing this element node.
     */

    public final String getSystemId() {
        return ((DocumentImpl) getOwnerDocument()).getSystemId();
    }

    /**
     * Get the base URI of this element node. This will be the same as the
     * System ID unless xml:base has been used.
     */

    public String getBaseURI() {
        return Navigator.getBaseURI(this);
    }

    /**
     * Get the nameCode of the node. This is used to locate the name in the
     * NamePool
     */

    public int getNameCode() {
        return nameCode;
    }

    /**
     * Return the type of node.
     * 
     * @return Node.ELEMENT
     */

    public final int getNodeKind() {
        return Node.ELEMENT;
    }

    /**
     * Set the namespace declarations for the element
     * 
     * @param namespaces
     *            the list of namespace codes
     * @param namespacesUsed
     *            the number of entries in the list that are used
     */

    public void setNamespaceDeclarations(int[] namespaces, int namespacesUsed) {
        namespaceList = new int[namespacesUsed];
        System.arraycopy(namespaces, 0, namespaceList, 0, namespacesUsed);
    }

    /**
     * Set the attribute collection for the element, for internal use only
     * 
     * @param namespaces
     *            the list of namespace codes
     * @param namespacesUsed
     *            the number of entries in the list that are used
     */
    public void setAttributes(AttributeCollectionImpl attributeList) {
        this.attributeList = attributeList;
    }

    /**
     * Get the namespace URI corresponding to a given prefix. Return null if the
     * prefix is not in scope.
     * 
     * @param prefix
     *            the namespace prefix. May be the zero-length string,
     *            indicating that there is no prefix. This indicates either the
     *            default namespace or the null namespace, depending on the
     *            value of useDefault.
     * @param useDefault
     *            true if the default namespace is to be used when the prefix is
     *            "". If false, the method returns "" when the prefix is "".
     * @return the uri for the namespace, or null if the prefix is not in scope.
     *         The "null namespace" is represented by the pseudo-URI "".
     */

    public String getURIForPrefix(String prefix, boolean useDefault) {
        if (prefix.equals("xml")) {
            return NamespaceConstant.XML;
        }
        if (prefix.length() == 0 && !useDefault) {
            return "";
        }

        NamePool pool = getNamePool();
        int prefixCode = pool.getCodeForPrefix(prefix);
        if (prefixCode == -1) {
            return null;
        }
        try {
            short uriCode = getURICodeForPrefixCode(prefixCode);
            return pool.getURIFromURICode(uriCode);
        } catch (NamespaceException e) {
            return null;
        }
    }

    /**
     * Get an iterator over all the prefixes declared in this namespace context.
     * This will include the default namespace (prefix="") and the XML namespace
     * where appropriate
     */

    public Iterator iteratePrefixes() {
        return new Iterator() {
            private NamePool pool = null;
            private IntIterator iter = NamespaceCodeIterator
                    .iterateNamespaces(ElementImpl.this);

            public boolean hasNext() {
                return (pool == null || iter.hasNext());
            }

            public Object next() {
                if (pool == null) {
                    pool = getNamePool();
                    return "xml";
                } else {
                    return pool.getPrefixFromNamespaceCode(iter.next());
                }
            }

            public void remove() {
                throw new UnsupportedOperationException("remove");
            }
        };
    }

    /**
     * Search the NamespaceList for a given prefix, returning the corresponding
     * URI.
     * 
     * @param prefix
     *            The prefix to be matched. To find the default namespace,
     *            supply ""
     * @return The URI code corresponding to this namespace. If it is an unnamed
     *         default namespace, return Namespace.NULL_CODE.
     * @throws NamespaceException
     *             if the prefix has not been declared on this NamespaceList.
     */

    public short getURICodeForPrefix(String prefix) throws NamespaceException {
        if (prefix.equals("xml"))
            return NamespaceConstant.XML_CODE;

        NamePool pool = getNamePool();
        int prefixCode = pool.getCodeForPrefix(prefix);
        if (prefixCode == -1) {
            throw new NamespaceException(prefix);
        }
        return getURICodeForPrefixCode(prefixCode);
    }

    private short getURICodeForPrefixCode(int prefixCode)
            throws NamespaceException {
        if (namespaceList != null) {
            for (int i = 0; i < namespaceList.length; i++) {
                if ((namespaceList[i] >> 16) == prefixCode) {
                    return (short) (namespaceList[i] & 0xffff);
                }
            }
        }
        Node next = parent;
        while (next != null) {
            if (next.getNodeKind() == Node.DOCUMENT) {
                // prefixCode==0 represents the empty namespace prefix ""
                if (prefixCode == 0)
                    return NamespaceConstant.NULL_CODE;
                throw new NamespaceException(getNamePool()
                        .getPrefixFromNamespaceCode(prefixCode << 16));
            } else if (next instanceof ElementImpl) {
                return ((ElementImpl) next).getURICodeForPrefixCode(prefixCode);
            } else {
                next = next.getParentNode();
            }
        }

        throw new NamespaceException(getNamePool().getPrefixFromNamespaceCode(
                prefixCode << 16));
    }

    /**
     * Search the NamespaceList for a given URI, returning the corresponding
     * prefix.
     * 
     * @param uri
     *            The URI to be matched.
     * @return The prefix corresponding to this URI. If not found, return null.
     *         If there is more than one prefix matching the URI, the first one
     *         found is returned. If the URI matches the default namespace,
     *         return an empty string.
     */

    public String getPrefixForURI(String uri) {
        if (uri.equals(NamespaceConstant.XML))
            return "xml";

        NamePool pool = getNamePool();
        int uriCode = pool.getCodeForURI(uri);
        if (uriCode < 0)
            return null;
        return getPrefixForURICode(uriCode);
    }

    private String getPrefixForURICode(int code) {
        if (namespaceList != null) {
            for (int i = 0; i < namespaceList.length; i++) {
                if ((namespaceList[i] & 0xffff) == code) {
                    return getNamePool().getPrefixFromNamespaceCode(
                            namespaceList[i]);
                }
            }
        }
        Node next = parent;
        while (next != null) {
            if (next instanceof Document) {
                return null;
            } else if (next instanceof ElementImpl) {
                return ((ElementImpl) next).getPrefixForURICode(code);
            } else {
                next = next.getParentNode();
            }
        }

        return null;
    }

    /**
     * Get all namespace undeclarations and undeclarations defined on this
     * element.
     * 
     * @param buffer
     *            If this is non-null, and the result array fits in this buffer,
     *            then the result may overwrite the contents of this array, to
     *            avoid the cost of allocating a new array on the heap.
     * @return An array of integers representing the namespace declarations and
     *         undeclarations present on this element. For a node other than an
     *         element, return null. Otherwise, the returned array is a sequence
     *         of namespace codes, whose meaning may be interpreted by reference
     *         to the name pool. The top half word of each namespace code
     *         represents the prefix, the bottom half represents the URI. If the
     *         bottom half is zero, then this is a namespace undeclaration
     *         rather than a declaration. The XML namespace is never included in
     *         the list. If the supplied array is larger than required, then the
     *         first unused entry will be set to -1.
     *         <p/>
     *         <p>
     *         For a node other than an element, the method returns null.
     *         </p>
     */

    public int[] getDeclaredNamespaces(int[] buffer) {
        return (namespaceList == null ? EMPTY_INT_ARRAY : namespaceList);
    }

    /**
     * Get the list of in-scope namespaces for this element as an array of
     * namespace codes. (Used by LiteralResultElement)
     * 
     * @return the list of namespaces
     */

    public int[] getInScopeNamespaceCodes() {
        return NamespaceCodeIterator.getInScopeNamespaceCodes(this);
    }

    /**
     * Returns whether this node (if it is an element) has any attributes.
     */

    public boolean hasAttributes() {
        return (this.attributeList != null && this.attributeList.getLength() > 0);
    }

    /**
     * Get the attribute list for this element.
     * 
     * @return The attribute list. This will not include any namespace
     *         attributes. The attribute names will be in expanded form, with
     *         prefixes replaced by URIs
     */

    public AttributeCollection getAttributes() {
        return attributeList;
    }

    /**
     * Get the value of a given attribute of this node
     * 
     * @param fingerprint
     *            The fingerprint of the attribute name
     * @return the attribute value if it exists or null if not
     */

    public String getAttributeValue(int fingerprint) {
        if (attributeList == null)
            return null;

        return attributeList.getValueByFingerprint(fingerprint);
    }

    /**
     * Get the attribute node of a given fingerprint of this node
     * 
     * @param fingerprint
     *            The fingerprint of the attribute name
     * @return the attribute node if it exists or null if not
     */

    public Attribute getAttributeNode(int fingerprint) {
        if (attributeList == null)
            return null;

        return attributeList.getAttributeNode(fingerprint);
    }

    /**
     * Remove an existing attribute node and return the removed attribute
     * 
     */
    public Attribute removeAttributeNode(Attribute oldAttr) {
        if (attributeList == null)
            return null;

        Attribute attr = attributeList.removeAttributeNode(oldAttr);
        if (attr == null) {
            // attr not found
            return null;
        }
        notifyAttrReplaced(null, attr);
        return attr;
    }

    /**
     * Removes an attribute by local name and namespace URI
     * 
     */
    public Attribute removeAttribute(String namespaceURI, String localName) {
        if (localName == null)
            return null; // invalid parameter

        if (this.attributeList == null)
            return null; // no attribute

        Attribute attr = attributeList.removeAttributeNode(namespaceURI,
                localName);
        if (attr == null) {
            // attr not found
            return null;
        }
        notifyAttrReplaced(null, attr);
        return attr;
    }

    /**
     * Adds a new attribute.
     * 
     */
    public Attribute setAttributeNode(Attribute newAttr) throws XMLException {
        if (newAttr == null)
            return null; // nothing to do

        AttributeImpl attr = (AttributeImpl) newAttr;
        Element owner = attr.getOwnerElement();
        if (owner != null) {
            if (owner != this)
                throw new XMLException(
                        "attribute node is in used by other element.");
        }

        String name = newAttr.getLocalPart();
        String uri = newAttr.getURI();

        Attribute oldAttr = null;
        try {
            oldAttr = removeAttribute(uri, name);
        } catch (Exception e) {
            // no old attribute found may be
        }
        appendAttributeNode(attr);
        return oldAttr;
    }

    /**
     * Adds a new attribute.
     * 
     */
    public Attribute setAttribute(String namespaceURI, String qualifiedName,
            String value) throws XMLException {
        if (qualifiedName == null)
            return null;

        int index = qualifiedName.indexOf(':');
        String localName = index != -1 ? qualifiedName.substring(index + 1)
                : qualifiedName;

        Attribute attr = getAttributeNode(namespaceURI, localName);
        if (attr != null) {
            attr.setValue(value); // change value
            return attr;
        } else {
            // new attribute
            Document doc = getOwnerDocument();
            if (doc == null)
                return null;

            String prefix = NameChecker.getPrefix(qualifiedName);
            AttributeImpl newAttr = createAttributeNode(prefix, namespaceURI,
                    localName, value);
            if (newAttr != null) {
                appendAttributeNode(newAttr);
            }

            return newAttr;
        }
    }

    /**
     * appendAttibuteNode method
     * 
     * @return org.w3c.dom.Attr
     * @param newAttr
     *            org.w3c.dom.Attr
     */
    public Attribute appendAttributeNode(Attribute newAttr) {
        if (newAttr == null)
            return null;
        AttributeImpl attr = (AttributeImpl) newAttr;

        // no need to check owner element
        // if (attr.getOwnerElement() != null)
        // return null;

        if (attributeList == null)
            attributeList = new AttributeCollectionImpl(getNamePool(), this);

        attributeList.addAttribute(attr);
        attr.setOwnerElement(this);

        notifyAttrReplaced(attr, null);
        return attr;
    }

    /**
     * repalceAttibuteNode method
     * 
     * @return org.w3c.dom.Attr
     * @param oldAttr
     *            org.w3c.dom.Attr
     * @param newAttr
     *            org.w3c.dom.Attr
     */
    public Attribute replaceAttributeNode(Attribute oldAttr, Attribute newAttr)
            throws XMLException {
        if (oldAttr == null) {
            // add new attribute
            return setAttributeNode(newAttr);
        }

        // remove the old
        Attribute removed = removeAttributeNode(oldAttr);

        if (newAttr != null)
            setAttributeNode(newAttr);

        return removed;
    }

    /**
     * notifyAttrReplaced method
     * 
     * @param newAttr
     *            org.w3c.dom.Attr
     * @param oldAttr
     *            org.w3c.dom.Attr
     */
    protected void notifyAttrReplaced(Attribute newAttr, Attribute oldAttr) {
        DocumentImpl document = (DocumentImpl) getContainerDocument();
        if (document == null)
            return;
        IDOMModel model = (IDOMModel) document.getStructuredModel();
        if (model == null)
            return;
        model.attrReplaced(this, newAttr, oldAttr);
    }

    public AttributeImpl createAttributeNode(String prefix,
            String namespaceURI, String localName, String value) {
        DocumentImpl doc = (DocumentImpl) getOwnerDocument();
        if (prefix == null)
            prefix = "";

        int nameCode = getNamePool().allocate(prefix, namespaceURI, localName);
        AttributeImpl newAttr = new AttributeImpl(doc, this, nameCode,
                StandardNames.XS_UNTYPED, 0, value);

        return newAttr;
    }

    public AttributeImpl createAttributeNode(String namespaceURI,
            String qualifiedName, String value) {
        DocumentImpl doc = (DocumentImpl) getOwnerDocument();

        int index = qualifiedName.indexOf(':');
        String localName = index != -1 ? qualifiedName.substring(index + 1)
                : qualifiedName;
        String prefix = NameChecker.getPrefix(qualifiedName);

        int nameCode = getNamePool().allocate(prefix, namespaceURI, localName);
        AttributeImpl newAttr = new AttributeImpl(doc, this, nameCode,
                StandardNames.XS_UNTYPED, 0, value);

        return newAttr;
    }

    public AttributeImpl getAttributeNode(String namespaceURI, String localName) {
        if (attributeList == null)
            return null;

        return (AttributeImpl) attributeList.getAttributeNode(namespaceURI,
                localName);
    }

    public void removeNamespace(String prefix, String uri) {
        if (prefix.equals("xmlns")) {
            // the binding xmlns:xmlns="http://www.w3.org/2000/xmlns/"
            // should never be reported, but it's been known to happen
            return;
        }

        NamePool namePool = getNamePool();

        // check whether the prefix binding has exists
        int namespaceCode = namePool.getNamespaceCode(prefix, uri);
        if (namespaceCode != -1) {
            // check its existence
            int prefixCode = namePool
                    .getPrefixCodeFromNamespaceCode(namespaceCode);
            try {
                int uriCode = getURICodeForPrefixCode(prefixCode);
                if (uriCode == namePool
                        .getURICodeFromNamespaceCode(namespaceCode)) {
                    removeNamespaceCode(namespaceCode);
                }
            } catch (NamespaceException e) {
                // not declared
            }
        }
    }

    public boolean addNamespace(String prefix, String uri) {
        if (prefix.equals("xmlns")) {
            // the binding xmlns:xmlns="http://www.w3.org/2000/xmlns/"
            // should never be reported, but it's been known to happen
            return false;
        }

        NamePool namePool = getNamePool();

        // check whether the prefix binding has exists
        int namespaceCode = namePool.getNamespaceCode(prefix, uri);
        if (namespaceCode != -1) {
            // check its existence
            int prefixCode = namePool
                    .getPrefixCodeFromNamespaceCode(namespaceCode);
            try {
                int uriCode = getURICodeForPrefixCode(prefixCode);
                if (uriCode == namePool
                        .getURICodeFromNamespaceCode(namespaceCode))
                    return false; // already exists
            } catch (NamespaceException e) {
                // not declared
            }
        } else {
            namespaceCode = namePool.allocateNamespaceCode(prefix, uri);
        }

        addNamespaceCode(namespaceCode);
        return true;
    }

    public boolean addNamespace(int nameCode) {
        NamePool namePool = getNamePool();

        // add necessary namespace
        int namespaceCode = namePool.getNamespaceCode(nameCode);
        if (namespaceCode != -1) {
            // check its existence
            int prefixCode = namePool
                    .getPrefixCodeFromNamespaceCode(namespaceCode);
            try {
                int uriCode = getURICodeForPrefixCode(prefixCode);
                if (uriCode == namePool
                        .getURICodeFromNamespaceCode(namespaceCode))
                    return false; // already exists
            } catch (NamespaceException e) {
                // not declared
            }
        } else {
            namespaceCode = namePool.allocateNamespaceCode(nameCode);
        }

        addNamespaceCode(namespaceCode);
        return true;
    }

    protected void removeNamespaceCode(int namespaceCode) {
        if (namespaceList != null) {
            int namespacesUsed = namespaceList.length;
            if (namespacesUsed == 1) {
                namespaceList = null;
                return;
            } else {
                int[] n2 = new int[namespacesUsed - 1];
                int i = 0;
                for (int n : namespaceList) {
                    if (n != namespaceCode) {
                        n2[i] = n;
                        i++;
                    }
                }
                namespaceList = n2;
            }
        }
    }

    protected void addNamespaceCode(int namespaceCode) {
        if (namespaceList != null) {
            int namespacesUsed = namespaceList.length;
            int[] n2 = new int[namespacesUsed + 1];
            System.arraycopy(namespaceList, 0, n2, 0, namespacesUsed);
            namespaceList = n2;

            namespaceList[namespacesUsed] = namespaceCode;
        } else {
            int namespacesUsed = 0;
            namespaceList = new int[namespacesUsed + 1];
            namespaceList[namespacesUsed] = namespaceCode;
        }
    }

    public boolean fixupNamespace() {
        return addNamespace(nameCode);
    }

    public boolean setName(String prefix, String uri, String localName) {
        NamePool namePool = getNamePool();
        int nameCode = namePool.allocate(prefix, uri, localName);
        if (nameCode == -1)
            return false;

        setNameCode(nameCode);
        return true;
    }

    /**
     * Set the name code. Used when creating a dummy element in the Stripper
     * 
     * @param nameCode
     *            the integer name code representing the element name
     */

    public void setNameCode(int nameCode) {
        this.nameCode = nameCode;

        notifyNameChanged();
    }

    public void notifyNameChanged() {
        DocumentImpl document = (DocumentImpl) getContainerDocument();
        if (document == null)
            return;
        IDOMModel model = (IDOMModel) document.getStructuredModel();
        if (model == null)
            return;
        model.nameChanged(this);
    }

    /**
     * Set the namespace declarations for the element
     * 
     * @param namespaces
     *            the list of namespace codes
     * @param namespacesUsed
     *            the number of entries in the list that are used
     */

    public void replaceNamespaceDeclarations(int[] namespaces,
            int namespacesUsed) {
        setNamespaceDeclarations(namespaces, namespacesUsed);
        notifyNamespaceChanged();
    }

    public void notifyNamespaceChanged() {
        DocumentImpl document = (DocumentImpl) getContainerDocument();
        if (document == null)
            return;
        IDOMModel model = (IDOMModel) document.getStructuredModel();
        if (model == null)
            return;
        model.namespaceChanged(this);
    }

    public Node cloneNode(boolean deep) {
        ElementImpl cloned = new ElementImpl(this);

        if (deep)
            cloneChildNodes(cloned, deep);

        return cloned;
    }

    /**
     * cloneAttributes method
     * 
     * @param newOwner
     *            Element
     */
    protected void cloneAttributes(Element newOwner) {
        if (newOwner == null || newOwner == this)
            return;

        ElementImpl element = (ElementImpl) newOwner;

        // assume the new owner has an empty attribute list
        if (attributeList == null) {
            element.attributeList = null;
            return;
        }

        attributeList.cloneAttributes(element);
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
