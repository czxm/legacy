package com.intel.cedar.engine.model.loader;

import com.intel.cedar.engine.xml.Configuration;
import com.intel.cedar.engine.xml.NamePool;
import com.intel.cedar.engine.xml.StandardNames;
import com.intel.cedar.engine.xml.loader.DefaultNodeFactory;
import com.intel.cedar.engine.xml.loader.NodeFactory;
import com.intel.cedar.engine.xml.model.Attribute;
import com.intel.cedar.engine.xml.model.AttributeImpl;
import com.intel.cedar.engine.xml.model.DocumentImpl;
import com.intel.cedar.engine.xml.model.Element;
import com.intel.cedar.engine.xml.model.ElementImpl;
import com.intel.cedar.engine.xml.model.ParentNodeImpl;
import com.intel.cedar.engine.xml.model.TextImpl;

public class ModelSerializer {
    protected Configuration config;
    protected DocumentImpl document;
    protected NamePool namePool;
    protected NodeFactory nodeFacotory = new DefaultNodeFactory();

    public ModelSerializer() {
        initialize();
    }

    protected void initialize() {
        document = new DocumentImpl();
        config = new Configuration();
        document.setConfiguration(config);
        namePool = document.getNamePool();
    }

    public static String integerToString(int value) {
        return new Integer(value).toString();
    }

    public static String booleanToString(boolean value) {
        return new Boolean(value).toString();
    }

    public TextImpl createTextNode(Element parent, String content) {
        TextImpl textNode = new TextImpl(document, content);

        parent.appendChild(textNode);

        return textNode;

    }

    protected Element createElement(String prefix, String namespaceURI,
            String localName) {
        int nameCode = namePool.allocate(prefix, namespaceURI, localName);
        return createElement(nameCode);
    }

    protected Element createElement(int nameCode) {
        ElementImpl elementNode = nodeFacotory.makeElementNode(document,
                nameCode, null, null, 0, null, -1);
        return elementNode;
    }

    protected Element createElement(ParentNodeImpl parent, String prefix,
            String namespaceURI, String localName) {
        Element element = createElement(prefix, namespaceURI, localName);
        if (element == null)
            return element;

        parent.appendChild(element);
        return element;
    }

    protected Element createElement(ParentNodeImpl parent, int fingerprint) {
        String namespaceURI = StandardNames.getURI(fingerprint);
        if (namespaceURI == null)
            return null;

        String localName = StandardNames.getLocalName(fingerprint);
        if (localName == null)
            return null;

        return createElement(parent, "", namespaceURI, localName);
    }

    protected Element createElement(Element parent, int fingerprint) {
        return createElement((ParentNodeImpl) parent, fingerprint);
    }

    protected Attribute createAttribute(Element element, String prefix,
            String namespaceURI, String localName, String value) {
        int nameCode = namePool.allocate(prefix, namespaceURI, localName);
        AttributeImpl attributeNode = new AttributeImpl(document,
                (ElementImpl) element, nameCode, StandardNames.XS_UNTYPED, 0,
                value);
        ((ElementImpl) element).appendAttributeNode(attributeNode);
        return attributeNode;
    }

    protected Attribute createAttribute(Element element, int fingerprint,
            String value) {
        String namespaceURI = StandardNames.getURI(fingerprint);
        if (namespaceURI == null)
            return null;

        String localName = StandardNames.getLocalName(fingerprint);
        if (localName == null)
            return null;

        return createAttribute(element, "", namespaceURI, localName, value);
    }

}
