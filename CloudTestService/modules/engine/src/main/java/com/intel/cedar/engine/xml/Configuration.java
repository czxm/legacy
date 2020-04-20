package com.intel.cedar.engine.xml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.URIResolver;

import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import com.intel.cedar.engine.xml.iterator.AnyNodeTest;
import com.intel.cedar.engine.xml.iterator.NodeTest;
import com.intel.cedar.engine.xml.model.Node;
import com.intel.cedar.engine.xml.type.BuiltInType;
import com.intel.cedar.engine.xml.type.SchemaDeclaration;
import com.intel.cedar.engine.xml.type.SchemaType;
import com.intel.cedar.engine.xml.type.TypeHierarchy;
import com.intel.cedar.engine.xml.type.Untyped;
import com.intel.cedar.engine.xml.util.Name10Checker;
import com.intel.cedar.engine.xml.util.Name11Checker;
import com.intel.cedar.engine.xml.util.NameChecker;
import com.intel.cedar.engine.xml.util.Whitespace;

/**
 * This class holds details of user-selected configuration options
 */

public class Configuration implements Serializable {
    /**
     * Constant indicating the XML Version 1.0
     */

    public static final int XML10 = 10;

    /**
     * Constant indicating the XML Version 1.1
     */

    public static final int XML11 = 11;

    /**
     * Constant indicating that the "host language" is XML Schema
     */
    public static final int XML_SCHEMA = 52;

    /**
     * Constant indicating that the host language is Java: that is, this is a
     * free-standing Java application with no XSLT or XQuery content
     */
    public static final int JAVA_APPLICATION = 53;

    /**
     * Constant indicating that the host language is XPATH itself - that is, a
     * free-standing XPath environment
     */
    public static final int XPATH = 54;

    private int xmlVersion = XML10;
    private boolean timing = false;
    private boolean validation = false;
    private int stripsWhiteSpace = Whitespace.IGNORABLE;
    private NamePool namePool = null;

    private transient List sourceParserPool = new ArrayList(5);
    private transient List styleParserPool = new ArrayList(5);

    private transient URIResolver uriResolver;
    private StandardURIResolver systemURIResolver = new StandardURIResolver(
            this);

    private transient TypeHierarchy typeHierarchy;

    /**
     * Create a non-schema-aware configuration object with default settings for
     * all options.
     * 
     * @since 8.4
     */

    public Configuration() {
        init();
    }

    public static Configuration makeConfiguration() {
        return new Configuration();
    }

    protected void init() {
        namePool = new NamePool();
    }

    /**
     * Copy an existing Configuration to create a new Configuration. This is a
     * shallow copy. The new Configuration will share all the option settings of
     * the old; it will also share the same NamePool, and the same
     * DocumentNumberAllocator. If this configuration is schema-aware then the
     * new one will also be schema-aware, and will share the same Schema manager
     * and so on. (So any schema component loaded into one configuration will
     * affect both).
     * 
     * <p>
     * Note that creating a new SchemaAwareConfiguration using this method can
     * be significantly cheaper than creating one from scratch, because it
     * avoids the need to verify the Saxon-SA license key if this has already
     * been done.
     * </p>
     * 
     * @return a shallow copy of this Configuration
     */

    public Configuration copy() {
        Configuration c = new Configuration();
        copyTo(c);
        return c;
    }

    protected void copyTo(Configuration c) {
        c.xmlVersion = xmlVersion;
        c.timing = timing;
        c.validation = validation;
        c.stripsWhiteSpace = stripsWhiteSpace;
        c.namePool = namePool;
        c.uriResolver = uriResolver;
        c.systemURIResolver = systemURIResolver;
        c.typeHierarchy = typeHierarchy;
    }

    /**
     * Set the XML version to be used by default for validating characters and
     * names. Note that source documents specifying xml version="1.0" or "1.1"
     * are accepted regardless of this setting. The effect of this switch is to
     * change the validation rules for types such as Name and NCName, to change
     * the meaning of \i and \c in regular expressions, and to determine whether
     * the serializer allows XML 1.1 documents to be constructed.
     * 
     * @param version
     *            one of the constants XML10 or XML11
     * @since 8.6
     */

    public void setXMLVersion(int version) {
        xmlVersion = version;
    }

    /**
     * Get the XML version to be used by default for validating characters and
     * names
     * 
     * @return one of the constants {@link #XML10} or {@link #XML11}
     * @since 8.6
     */

    public int getXMLVersion() {
        return xmlVersion;
    }

    /**
     * Get a class that can be used to check names against the selected XML
     * version
     * 
     * @return a class that can be used for name checking
     * @since 8.6
     */

    public NameChecker getNameChecker() {
        // noinspection RedundantCast
        return (xmlVersion == XML10 ? (NameChecker) Name10Checker.getInstance()
                : (NameChecker) Name11Checker.getInstance());
    }

    /**
     * Determine whether brief progress messages and timing information will be
     * output to System.err.
     * <p>
     * This method is provided largely for internal use. Progress messages are
     * normally controlled directly from the command line interfaces, and are
     * not normally used when driving Saxon from the Java API.
     * 
     * @return true if these messages are to be output.
     */

    public boolean isTiming() {
        return timing;
    }

    /**
     * Determine whether brief progress messages and timing information will be
     * output to System.err.
     * <p>
     * This method is provided largely for internal use. Progress messages are
     * normally controlled directly from the command line interfaces, and are
     * not normally used when
     * 
     * @param timing
     *            true if these messages are to be output.
     */

    public void setTiming(boolean timing) {
        this.timing = timing;
    }

    /**
     * Determine whether the XML parser for source documents will be asked to
     * perform \ validation of source documents
     * 
     * @return true if DTD validation is requested.
     * @since 8.4
     */

    public boolean isValidation() {
        return validation;
    }

    /**
     * Determine whether the XML parser for source documents will be asked to
     * perform DTD validation of source documents
     * 
     * @param validation
     *            true if DTD validation is to be requested.
     * @since 8.4
     */

    public void setValidation(boolean validation) {
        this.validation = validation;
    }

    /**
     * Get the target namepool to be used for stylesheets/queries and for source
     * documents.
     * 
     * @return the target name pool. If no NamePool has been specified
     *         explicitly, the default NamePool is returned.
     * @since 8.4
     */

    public NamePool getNamePool() {
        return namePool;
    }

    /**
     * Set the NamePool to be used for stylesheets/queries and for source
     * documents.
     * 
     * <p>
     * Using this method allows several Configurations to share the same
     * NamePool. This was the normal default arrangement until Saxon 8.9, which
     * changed the default so that each Configuration uses its own NamePool.
     * </p>
     * 
     * <p>
     * Sharing a NamePool creates a potential bottleneck, since changes to the
     * namepool are synchronized.
     * </p>
     * 
     * @param targetNamePool
     *            The NamePool to be used.
     * @since 8.4
     */

    public void setNamePool(NamePool targetNamePool) {
        namePool = targetNamePool;
    }

    /**
     * Determine whether whitespace-only text nodes are to be stripped
     * unconditionally from source documents.
     * 
     * @return true if all whitespace-only text nodes are stripped.
     * @since 8.4
     */

    public boolean isStripsAllWhiteSpace() {
        return stripsWhiteSpace == Whitespace.ALL;
    }

    /**
     * Determine whether whitespace-only text nodes are to be stripped
     * unconditionally from source documents.
     * 
     * @param stripsAllWhiteSpace
     *            if all whitespace-only text nodes are to be stripped.
     * @since 8.4
     */

    public void setStripsAllWhiteSpace(boolean stripsAllWhiteSpace) {
        if (stripsAllWhiteSpace) {
            stripsWhiteSpace = Whitespace.ALL;
        }
    }

    /**
     * Set which kinds of whitespace-only text node should be stripped.
     * 
     * @param kind
     *            the kind of whitespace-only text node that should be stripped
     *            when building a source tree. One of {@link Whitespace#NONE}
     *            (none), {@link Whitespace#ALL} (all), or
     *            {@link Whitespace#IGNORABLE} (element-content whitespace as
     *            defined in a DTD or schema)
     */

    public void setStripsWhiteSpace(int kind) {
        stripsWhiteSpace = kind;
    }

    /**
     * Set which kinds of whitespace-only text node should be stripped.
     * 
     * @return kind the kind of whitespace-only text node that should be
     *         stripped when building a source tree. One of
     *         {@link Whitespace#NONE} (none), {@link Whitespace#ALL} (all), or
     *         {@link Whitespace#IGNORABLE} (element-content whitespace as
     *         defined in a DTD or schema)
     */

    public int getStripsWhiteSpace() {
        return stripsWhiteSpace;
    }

    /**
     * Get a parser for source documents. The parser is allocated from a pool if
     * any are available from the pool: the client should ideally return the
     * parser to the pool after use, so that it can be reused.
     * <p>
     * This method is intended primarily for internal use.
     * 
     * @return a parser, in which the namespace properties must be set as
     *         follows: namespaces=true; namespace-prefixes=false. The DTD
     *         validation feature of the parser will be set on or off depending
     *         on the {@link #setValidation(boolean)} setting.
     */

    public XMLReader getSourceParser() throws XMLException {
        if (sourceParserPool == null) {
            sourceParserPool = new ArrayList(10);
        }
        if (!sourceParserPool.isEmpty()) {
            int n = sourceParserPool.size() - 1;
            XMLReader parser = (XMLReader) sourceParserPool.get(n);
            sourceParserPool.remove(n);
            return parser;
        }
        XMLReader parser = loadParser();
        try {
            configureParser(parser);
        } catch (XMLException err) {
            throw err;
        }
        if (isValidation()) {
            try {
                parser.setFeature("http://xml.org/sax/features/validation",
                        true);
            } catch (SAXException err) {
                throw new XMLException(
                        "The XML parser does not support validation");
            }
        }

        return parser;
    }

    /**
     * Return a source parser to the pool, for reuse
     * 
     * @param parser
     *            The parser: the caller must not supply a parser that was
     *            obtained by any mechanism other than calling the
     *            getSourceParser() method.
     */

    public synchronized void reuseSourceParser(XMLReader parser) {
        if (sourceParserPool == null) {
            sourceParserPool = new ArrayList(10);
        }
        try {
            // give things back to the garbage collecter
            parser.setContentHandler(null);
            parser.setEntityResolver(null);
            parser.setDTDHandler(null);
            parser.setErrorHandler(null);
        } catch (Exception err) {
            //
        }
        sourceParserPool.add(parser);
    }

    /**
     * Get a parser by instantiating the SAXParserFactory
     * 
     * @return the parser (XMLReader)
     */

    private XMLReader loadParser() throws XMLException {
        XMLReader parser;
        try {
            parser = SAXParserFactory.newInstance().newSAXParser()
                    .getXMLReader();
        } catch (ParserConfigurationException err) {
            throw new XMLException(err);
        } catch (SAXException err) {
            throw new XMLException(err);
        }
        return parser;
    }

    /**
     * Get the parser for stylesheet documents. This parser is also used for
     * schema documents.
     * <p>
     * This method is intended for internal use only.
     * 
     * @return an XML parser (a SAX2 parser) that can be used for stylesheets
     *         and schema documents
     * 
     */

    public synchronized XMLReader getStyleParser() throws XMLException {
        if (styleParserPool == null) {
            styleParserPool = new ArrayList(10);
        }
        if (!styleParserPool.isEmpty()) {
            int n = styleParserPool.size() - 1;
            XMLReader parser = (XMLReader) styleParserPool.get(n);
            styleParserPool.remove(n);
            return parser;
        }
        XMLReader parser = loadParser();
        try {
            parser.setFeature("http://xml.org/sax/features/namespaces", true);
            parser.setFeature("http://xml.org/sax/features/namespace-prefixes",
                    false);
        } catch (SAXNotRecognizedException e) {
            throw new XMLException(e);
        } catch (SAXNotSupportedException e) {
            throw new XMLException(e);
        }
        return parser;
    }

    /**
     * Return a stylesheet (or schema) parser to the pool, for reuse
     * 
     * @param parser
     *            The parser: the caller must not supply a parser that was
     *            obtained by any mechanism other than calling the
     *            getStyleParser() method.
     */

    public synchronized void reuseStyleParser(XMLReader parser) {
        if (styleParserPool == null) {
            styleParserPool = new ArrayList(10);
        }
        styleParserPool.add(parser);
    }

    /**
     * Configure a SAX parser to ensure it has the correct namesapce properties
     * set
     * 
     * @param parser
     *            the parser to be configured
     */

    public static void configureParser(XMLReader parser) throws XMLException {
        try {
            parser.setFeature("http://xml.org/sax/features/namespaces", true);
        } catch (SAXNotSupportedException err) { // SAX2 parsers MUST support
                                                 // this feature!
            throw new XMLException("The SAX2 parser "
                    + parser.getClass().getName()
                    + " does not recognize the 'namespaces' feature");
        } catch (SAXNotRecognizedException err) {
            throw new XMLException(
                    "The SAX2 parser "
                            + parser.getClass().getName()
                            + " does not support setting the 'namespaces' feature to true");
        }

        try {
            parser.setFeature("http://xml.org/sax/features/namespace-prefixes",
                    false);
        } catch (SAXNotSupportedException err) { // SAX2 parsers MUST support
                                                 // this feature!
            throw new XMLException("The SAX2 parser "
                    + parser.getClass().getName()
                    + " does not recognize the 'namespace-prefixes' feature");
        } catch (SAXNotRecognizedException err) {
            throw new XMLException(
                    "The SAX2 parser "
                            + parser.getClass().getName()
                            + " does not support setting the 'namespace-prefixes' feature to false");
        }

    }

    /**
     * Get the URIResolver used in this configuration
     * 
     * @return the URIResolver. If no URIResolver has been set explicitly, the
     *         default URIResolver is used.
     * @since 8.4
     */

    public URIResolver getURIResolver() {
        if (uriResolver == null) {
            return systemURIResolver;
        }
        return uriResolver;
    }

    /**
     * Set the URIResolver to be used in this configuration. This will be used
     * to resolve the URIs used statically (e.g. by xsl:include) and also the
     * URIs used dynamically by functions such as document() and doc(). Note
     * that the URIResolver does not resolve the URI in the sense of RFC 2396
     * (which is also the sense in which the resolve-uri() function uses the
     * term): rather it dereferences an absolute URI to obtain an actual
     * resource, which is returned as a Source object.
     * 
     * @param resolver
     *            The URIResolver to be used.
     * @since 8.4
     */

    public void setURIResolver(URIResolver resolver) {
        uriResolver = resolver;
        if (resolver instanceof StandardURIResolver) {
            ((StandardURIResolver) resolver).setConfiguration(this);
        }
    }

    /**
     * Get the system-defined URI Resolver. This is used when the user-defined
     * URI resolver returns null as the result of the resolve() method
     * 
     * @return the system-defined URI resolver
     */

    public StandardURIResolver getSystemURIResolver() {
        return systemURIResolver;
    }

    /**
     * Get the top-level schema type definition with a given fingerprint.
     * <p>
     * This method is intended for internal use and for use by advanced
     * applications. (The SchemaType object returned cannot yet be considered a
     * stable API, and may be superseded when a JAXP API for schema information
     * is defined.)
     * 
     * @param fingerprint
     *            the fingerprint of the schema type
     * @return the schema type , or null if there is none with this name.
     */

    public SchemaType getSchemaType(int fingerprint) {
        if (fingerprint < 1023) {
            return BuiltInType.getSchemaType(fingerprint);
        }
        return null;
    }

    /**
     * Get the TypeHierarchy: a cache holding type information
     * 
     * @return the type hierarchy cache
     */

    public final TypeHierarchy getTypeHierarchy() {
        if (typeHierarchy == null) {
            typeHierarchy = new TypeHierarchy(this);
        }
        return typeHierarchy;
    }

    /**
     * Determine whether calls to external Java functions are permitted.
     * 
     * @return true if such calls are permitted.
     * @since 8.4
     */

    public boolean isAllowExternalFunctions() {
        return true;
    }

    /**
     * Load a class using the class name provided. Note that the method does not
     * check that the object is of the right class.
     * <p>
     * This method is intended for internal use only.
     * 
     * @param className
     *            A string containing the name of the class, for example
     *            "com.microstar.sax.LarkDriver"
     * @param tracing
     *            true if diagnostic tracing is required
     * @param classLoader
     *            The ClassLoader to be used to load the class. If this is null,
     *            then the classLoader used will be the first one available of:
     *            the classLoader registered with the Configuration using
     *            {@link #setClassLoader}; the context class loader for the
     *            current thread; or failing that, the class loader invoked
     *            implicitly by a call of Class.forName() (which is the
     *            ClassLoader that was used to load the Configuration object
     *            itself).
     * @return an instance of the class named, or null if it is not loadable.
     * @throws Exception
     *             if the class cannot be loaded.
     * 
     */

    public Class getClass(String className, boolean tracing,
            ClassLoader classLoader) throws Exception {
        if (tracing) {
            System.err.println("Loading " + className);
        }

        try {
            ClassLoader loader = classLoader;
            if (loader == null) {
                loader = Thread.currentThread().getContextClassLoader();
            }
            if (loader != null) {
                try {
                    return loader.loadClass(className);
                } catch (Exception ex) {
                    return Class.forName(className);
                }
            } else {
                return Class.forName(className);
            }
        } catch (Exception e) {
            if (tracing) {
                // The exception is often masked, especially when calling
                // extension
                // functions
                System.err.println("No Java class " + className
                        + " could be loaded");
            }
            throw new Exception("Failed to load " + className, e);
        }

    }

    /**
     * Determine if the configuration is schema-aware, for the given host
     * language
     * 
     * @param language
     *            the required host language: XSLT, XQUERY, or XML_SCHEMA
     * @return true if the configuration is schema-aware
     * @since 8.4
     */

    public boolean isSchemaAware(int language) {
        return true;
        // changing this to true will do no good!
    }

    /**
     * Determine whether the Configuration contains a cached schema for a given
     * target namespace
     * 
     * @param targetNamespace
     *            the target namespace of the schema being sought (supply "" for
     *            the unnamed namespace)
     * @return true if the schema for this namespace is available, false if not.
     */

    public boolean isSchemaAvailable(String targetNamespace) {
        return false;
    }

    /**
     * Mark a schema namespace as being sealed. This is done when components
     * from this namespace are first used for validating a source document or
     * compiling a source document or query. Once a namespace has been sealed,
     * it is not permitted to change the schema components in that namespace by
     * redefining them, deriving new types by extension, or adding to their
     * substitution groups.
     * 
     * @param namespace
     *            the namespace URI of the components to be sealed
     */

    public void sealNamespace(String namespace) {
        //
    }

    /**
     * Get a global attribute declaration.
     * <p>
     * This method is intended for internal use
     * 
     * @param fingerprint
     *            the namepool fingerprint of the required attribute declaration
     * @return the attribute declaration whose name matches the given
     *         fingerprint, or null if no element declaration with this name has
     *         been registered.
     */

    public SchemaDeclaration getAttributeDeclaration(int fingerprint) {
        // TODO
        // resolve a imported schema attribute declaration
        return new DummySchemaDeclaration(fingerprint);
    }

    /**
     * Get a global element declaration.
     * <p>
     * This method is intended for internal use.
     * 
     * @param fingerprint
     *            the NamePool fingerprint of the name of the required element
     *            declaration
     * @return the element declaration whose name matches the given fingerprint,
     *         or null if no element declaration with this name has been
     *         registered.
     */

    public SchemaDeclaration getElementDeclaration(int fingerprint) {
        // TODO
        // resolve a imported schema element declaration
        return new DummySchemaDeclaration(fingerprint);
    }

    /**
     * Simple interface to load a schema document
     * 
     * @param absoluteURI
     *            the absolute URI of the location of the schema document
     */

    public void loadSchema(String absoluteURI) throws SchemaException {
        readSchema("", absoluteURI, null);
    }

    /**
     * Read a schema from a given schema location
     * <p>
     * This method is intended for internal use.
     * 
     * @param pipe
     *            the PipelineConfiguration
     * @param baseURI
     *            the base URI of the instruction requesting the reading of the
     *            schema
     * @param schemaLocation
     *            the location of the schema to be read
     * @param expected
     *            The expected targetNamespace of the schema being read.
     * @return the target namespace of the schema; null if there is no
     *         expectation
     * @throws UnsupportedOperationException
     *             when called in the non-schema-aware version of the product
     */

    public String readSchema(String baseURI, String schemaLocation,
            String expected) throws SchemaException {
        // TODO
        return expected;
    }

    /**
     * Read an inline schema from a stylesheet.
     * <p>
     * This method is intended for internal use.
     * 
     * @param root
     *            the xs:schema element in the stylesheet
     * @param expected
     *            the target namespace expected; null if there is no
     *            expectation.
     * @param errorListener
     *            The destination for error messages. May be null, in which case
     *            the errorListener registered with this Configuration is used.
     * @return the actual target namespace of the schema
     * 
     */

    public String readInlineSchema(Node root, String expected,
            ErrorListener errorListener) throws SchemaException {
        // TODO
        return expected;
    }

    protected class DummySchemaDeclaration implements SchemaDeclaration {
        int fingerprint;

        public DummySchemaDeclaration(int fingerprint) {
            this.fingerprint = fingerprint;
        }

        /**
         * Get the simple or complex type associated with the element or
         * attribute declaration
         * 
         * @return the simple or complex type
         */

        public SchemaType getType() {
            return Untyped.getInstance();
        }

        /**
         * Create a NodeTest that implements the semantics of
         * schema-element(name) or schema-attribute(name) applied to this
         * element or attribute declaration.
         */

        public NodeTest makeSchemaNodeTest() {
            return AnyNodeTest.getInstance();
        }

        /**
         * Determine, in the case of an Element Declaration, whether it is
         * nillable.
         */

        public boolean isNillable() {
            return true;
        }

        /**
         * Determine, in the case of an Element Declaration, whether the
         * declaration is abstract
         */

        public boolean isAbstract() {
            return false;
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