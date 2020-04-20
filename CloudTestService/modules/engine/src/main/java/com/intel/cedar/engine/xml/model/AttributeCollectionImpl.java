package com.intel.cedar.engine.xml.model;

import java.util.ArrayList;

import org.xml.sax.Attributes;

import com.intel.cedar.engine.xml.NamePool;
import com.intel.cedar.engine.xml.StandardNames;
import com.intel.cedar.engine.xml.util.Name11Checker;

/**
 * AttributeCollectionImpl is an implementation of both the SAX2 interface
 * Attributes and the Saxon equivalent AttributeCollection.
 * 
 * <p>
 * As well as providing the information required by the SAX2 interface, an
 * AttributeCollection can hold type information (as needed to support the JAXP
 * 1.3 {@link javax.xml.validation.ValidatorHandler} interface), and location
 * information for debugging. The location information is used in the case of
 * attributes on a result tree to identify the location in the query or
 * stylesheet from which they were generated.
 */

public final class AttributeCollectionImpl implements Attributes,
        AttributeCollection {

    // Attribute values are maintained as an array of Strings. Everything else
    // is maintained
    // in the form of integers.
    private ElementImpl owner = null;
    private ArrayList<AttributeImpl> attrs = null;
    private NamePool namePool;

    /**
     * Create an empty attribute list.
     * 
     * @param pool
     *            the NamePool
     */

    public AttributeCollectionImpl(NamePool pool) {
        namePool = pool;
    }

    public AttributeCollectionImpl(NamePool pool, ElementImpl owner) {
        this.namePool = pool;
        this.owner = owner;
    }

    public void setOwner(ElementImpl owner) {
        this.owner = owner;

        // set all the owner of the attributes
        if (attrs != null) {
            AttributeImpl attr;
            int size = attrs.size();
            for (int i = 0; i < size; i++) {
                attr = attrs.get(i);
                attr.setParent(owner);
                attr.setOwnerDocument(owner.getOwnerDocument());
            }
        }
    }

    /**
     * Add an attribute to an attribute list. The parameters correspond to the
     * parameters of the
     * {@link net.sf.saxon.event.Receiver#attribute(int,int,CharSequence,int,int)}
     * method. There is no check that the name of the attribute is distinct from
     * other attributes already in the collection: this check must be made by
     * the caller.
     * 
     * @param nameCode
     *            Integer representing the attribute name.
     * @param typeCode
     *            The attribute type code
     * @param value
     *            The attribute value (must not be null)
     * @param locationId
     *            Identifies the attribtue location.
     * @param properties
     *            Attribute properties
     */

    public void addAttribute(int nameCode, int typeCode, String value,
            int lineNumber, int properties) {
        if (attrs == null)
            attrs = new ArrayList<AttributeImpl>(5);

        AttributeImpl attr = new AttributeImpl(owner, nameCode, typeCode,
                properties, value);
        attr.setLineNumber(lineNumber);
        attrs.add(attr);
    }

    public void addAttribute(AttributeImpl attr) {
        if (attrs == null)
            attrs = new ArrayList<AttributeImpl>(5);

        attr.setOwnerElement(owner);
        attrs.add(attr);
    }

    /**
     * Set (overwrite) an attribute in the attribute list. The parameters
     * correspond to the parameters of the
     * {@link net.sf.saxon.event.Receiver#attribute(int,int,CharSequence,int,int)}
     * method.
     * 
     * @param index
     *            Identifies the entry to be replaced
     * @param nameCode
     *            Integer representing the attribute name.
     * @param typeCode
     *            The attribute type code
     * @param value
     *            The attribute value (must not be null)
     * @param locationId
     *            Identifies the attribtue location.
     * @param properties
     *            Attribute properties
     */

    public void setAttribute(int index, int nameCode, int typeCode,
            String value, long locationId, int properties) {
        AttributeImpl attr = getAttribute(index);
        if (attr == null)
            return;

        attr.setNameCode(nameCode);
        attr.setTypeCode(typeCode);
        attr.setValue(value);
        attr.setProperties(properties);
    }

    /**
     * Clear the attribute list. This removes the values but doesn't free the
     * memory used. free the memory, use clear() then compact().
     */

    public void clear() {
        attrs = null;
    }

    /**
     * Compact the attribute list to avoid wasting memory
     */

    public void compact() {

    }

    /**
     * Return the number of attributes in the list.
     * 
     * @return The number of attributes in the list.
     */

    public int getLength() {
        return (attrs == null ? 0 : attrs.size());
    }

    /**
     * Get the namecode of an attribute (by position).
     * 
     * @param index
     *            The position of the attribute in the list.
     * @return The display name of the attribute as a string, or null if there
     *         is no attribute at that position.
     */

    public int getNameCode(int index) {
        AttributeImpl attr = getAttribute(index);
        if (attrs == null) {
            return -1;
        }

        return attr.getNameCode();
    }

    /**
     * Get the namecode of an attribute (by position).
     * 
     * @param index
     *            The position of the attribute in the list.
     * @return The type annotation, as the fingerprint of the type name. The bit
     *         {@link net.sf.saxon.om.NodeInfo#IS_DTD_TYPE} represents a
     *         DTD-derived type.
     */

    public int getTypeAnnotation(int index) {
        AttributeImpl attr = getAttribute(index);
        if (attr == null) {
            return StandardNames.XS_UNTYPED_ATOMIC;
        }

        return attr.getTypeAnnotation();
    }

    /**
     * Get the properties of an attribute (by position)
     * 
     * @param index
     *            The position of the attribute in the list.
     * @return The properties of the attribute. This is a set of bit-settings
     *         defined in class {@link net.sf.saxon.event.ReceiverOptions}. The
     *         most interesting of these is {
     *         {@link net.sf.saxon.event.ReceiverOptions#DEFAULTED_ATTRIBUTE},
     *         which indicates an attribute that was added to an element as a
     *         result of schema validation.
     */

    public int getProperties(int index) {
        AttributeImpl attr = getAttribute(index);
        if (attr == null) {
            return -1;
        }

        return attr.getProperties();
    }

    /**
     * Get the prefix of the name of an attribute (by position).
     * 
     * @param index
     *            The position of the attribute in the list.
     * @return The prefix of the attribute name as a string, or null if there is
     *         no attribute at that position. Returns "" for an attribute that
     *         has no prefix.
     */

    public String getPrefix(int index) {
        AttributeImpl attr = getAttribute(index);
        if (attr == null) {
            return null;
        }

        return namePool.getPrefix(attr.getNameCode());
    }

    /**
     * Get the lexical QName of an attribute (by position).
     * 
     * @param index
     *            The position of the attribute in the list.
     * @return The lexical QName of the attribute as a string, or null if there
     *         is no attribute at that position.
     */

    public String getQName(int index) {
        AttributeImpl attr = getAttribute(index);
        if (attr == null) {
            return null;
        }

        return namePool.getDisplayName(attr.getNameCode());
    }

    /**
     * Get the local name of an attribute (by position).
     * 
     * @param index
     *            The position of the attribute in the list.
     * @return The local name of the attribute as a string, or null if there is
     *         no attribute at that position.
     */

    public String getLocalName(int index) {
        AttributeImpl attr = getAttribute(index);
        if (attr == null) {
            return null;
        }

        return namePool.getLocalName(attr.getNameCode());
    }

    /**
     * Get the namespace URI of an attribute (by position).
     * 
     * @param index
     *            The position of the attribute in the list.
     * @return The local name of the attribute as a string, or null if there is
     *         no attribute at that position.
     */

    public String getURI(int index) {
        AttributeImpl attr = getAttribute(index);
        if (attr == null) {
            return null;
        }

        return namePool.getURI(attr.getNameCode());
    }

    /**
     * Get the type of an attribute (by position). This is a SAX2 method, so it
     * gets the type name as a DTD attribute type, mapped from the schema type
     * code.
     * 
     * @param index
     *            The position of the attribute in the list.
     * @return The attribute type as a string ("NMTOKEN" for an enumeration, and
     *         "CDATA" if no declaration was read), or null if there is no
     *         attribute at that position.
     */

    public String getType(int index) {
        int typeCode = getTypeAnnotation(index) & NamePool.FP_MASK;
        switch (typeCode) {
        case StandardNames.XS_ID:
            return "ID";
        case StandardNames.XS_IDREF:
            return "IDREF";
        case StandardNames.XS_NMTOKEN:
            return "NMTOKEN";
        case StandardNames.XS_ENTITY:
            return "ENTITY";
        case StandardNames.XS_IDREFS:
            return "IDREFS";
        case StandardNames.XS_NMTOKENS:
            return "NMTOKENS";
        case StandardNames.XS_ENTITIES:
            return "ENTITIES";
        default:
            return "CDATA";
        }
    }

    /**
     * Get the type of an attribute (by name).
     * 
     * @param uri
     *            The namespace uri of the attribute.
     * @param localname
     *            The local name of the attribute.
     * @return The index position of the attribute
     */

    public String getType(String uri, String localname) {
        int index = findByName(uri, localname);
        return (index < 0 ? null : getType(index));
    }

    /**
     * Get the value of an attribute (by position).
     * 
     * @param index
     *            The position of the attribute in the list.
     * @return The attribute value as a string, or null if there is no attribute
     *         at that position.
     */

    public String getValue(int index) {
        AttributeImpl attr = getAttribute(index);
        if (attr == null) {
            return null;
        }

        return attr.getValue();
    }

    /**
     * Get the value of an attribute (by name).
     * 
     * @param uri
     *            The namespace uri of the attribute.
     * @param localname
     *            The local name of the attribute.
     * @return The index position of the attribute
     */

    public String getValue(String uri, String localname) {
        int index = findByName(uri, localname);
        return (index < 0 ? null : getValue(index));
    }

    /**
     * Get the attribute value using its fingerprint
     */

    public String getValueByFingerprint(int fingerprint) {
        int index = findByFingerprint(fingerprint);
        return (index < 0 ? null : getValue(index));
    }

    /**
     * Get the index of an attribute, from its lexical QName
     * 
     * @param qname
     *            The lexical QName of the attribute. The prefix must match.
     * @return The index position of the attribute
     */

    public int getIndex(String qname) {
        if (attrs == null) {
            return -1;
        }
        if (qname.indexOf(':') < 0) {
            return findByName("", qname);
        }
        // Searching using prefix+localname is not recommended, but SAX allows
        // it...
        String[] parts;
        try {
            parts = Name11Checker.getInstance().getQNameParts(qname);
        } catch (Exception err) {
            return -1;
        }
        String prefix = parts[0];
        if (prefix.length() == 0) {
            return findByName("", qname);
        } else {
            String localName = parts[1];
            int size = attrs.size();
            for (int i = 0; i < size; i++) {
                String lname = namePool.getLocalName(getNameCode(i));
                String ppref = namePool.getPrefix(getNameCode(i));
                if (localName.equals(lname) && prefix.equals(ppref)) {
                    return i;
                }
            }
            return -1;
        }
    }

    /**
     * Get the index of an attribute (by name).
     * 
     * @param uri
     *            The namespace uri of the attribute.
     * @param localname
     *            The local name of the attribute.
     * @return The index position of the attribute
     */

    public int getIndex(String uri, String localname) {
        return findByName(uri, localname);
    }

    /**
     * Get the index, given the fingerprint. Return -1 if not found.
     */

    public int getIndexByFingerprint(int fingerprint) {
        return findByFingerprint(fingerprint);
    }

    /**
     * Get the type of an attribute (by lexical QName).
     * 
     * @param name
     *            The lexical QName of the attribute.
     * @return The attribute type as a string (e.g. "NMTOKEN", or "CDATA" if no
     *         declaration was read).
     */

    public String getType(String name) {
        int index = getIndex(name);
        return getType(index);
    }

    /**
     * Get the value of an attribute (by lexical QName).
     * 
     * @param name
     *            The attribute name (a lexical QName). The prefix must match
     *            the prefix originally used. This method is defined in SAX, but
     *            is not recommended except where the prefix is null.
     */

    public String getValue(String name) {
        int index = getIndex(name);
        return getValue(index);
    }

    /**
     * Find an attribute by expanded name
     * 
     * @param uri
     *            the namespace uri
     * @param localName
     *            the local name
     * @return the index of the attribute, or -1 if absent
     */

    private int findByName(String uri, String localName) {
        if (namePool == null) {
            return -1; // indicates an empty attribute set
        }
        int f = namePool.getFingerprint(uri, localName);
        if (f == -1) {
            return -1;
        }
        return findByFingerprint(f);
    }

    /**
     * Find an attribute by fingerprint
     * 
     * @param fingerprint
     *            the fingerprint representing the name of the required
     *            attribute
     * @return the index of the attribute, or -1 if absent
     */

    private int findByFingerprint(int fingerprint) {
        if (attrs == null) {
            return -1;
        }

        int size = attrs.size();
        for (int i = 0; i < size; i++) {
            if (fingerprint == ((attrs.get(i).getNameCode()) & NamePool.FP_MASK)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Find an attribute by node reference
     * 
     * @return the index of the attribute, or -1 if absent
     */

    private int findByReference(Attribute attr) {
        int size = attrs.size();
        for (int i = 0; i < size; i++) {
            if (attrs.get(i) == attr) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Determine whether a given attribute has the is-ID property set
     */

    public boolean isId(int index) {
        return getType(index).equals("ID")
                || ((getNameCode(index) & NamePool.FP_MASK) == StandardNames.XML_ID);
    }

    /**
     * Determine whether a given attribute has the is-idref property set
     */

    public boolean isIdref(int index) {
        return false;
    }

    public AttributeImpl getAttribute(int index) {
        if (attrs == null) {
            return null;
        }
        if (index < 0 || index >= attrs.size()) {
            return null;
        }
        return attrs.get(index);
    }

    public Attribute removeAttributeNode(Attribute attr) {
        // find in the list
        if (attrs == null)
            return null;

        int index = findByReference(attr);
        if (index < 0)
            return null;

        // remove the attribute node
        attrs.remove(index);
        return attr;
    }

    public Attribute removeAttributeNode(String namespaceURI, String localName) {
        // find in the list
        if (attrs == null)
            return null;

        int index = findByName(namespaceURI, localName);
        if (index < 0)
            return null;

        Attribute attr = attrs.get(index);

        // remove the attribute node
        attrs.remove(index);
        return attr;
    }

    public Attribute getAttributeNode(String namespaceURI, String localName) {
        int index = findByName(namespaceURI, localName);
        if (index < 0)
            return null;

        return attrs.get(index);
    }

    /**
     * Get the attribute value using its fingerprint
     */

    public Attribute getAttributeNode(int fingerprint) {
        int index = findByFingerprint(fingerprint);
        if (index < 0)
            return null;

        return attrs.get(index);
    }

    /**
     * cloneAttributes method
     * 
     * @param newOwner
     *            Element
     */
    protected void cloneAttributes(ElementImpl newOwner) {

        AttributeCollectionImpl newAttributes = new AttributeCollectionImpl(
                namePool, newOwner);
        newOwner.setAttributes(newAttributes);
        if (attrs == null)
            return;

        int size = attrs.size();
        for (int i = 0; i < size; i++) {
            Attribute attr = attrs.get(i);
            if (attr == null)
                continue;

            AttributeImpl cloned = (AttributeImpl) attr.cloneNode(false);
            if (cloned != null)
                newAttributes.addAttribute(cloned);
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
