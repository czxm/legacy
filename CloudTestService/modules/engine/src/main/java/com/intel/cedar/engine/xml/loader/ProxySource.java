package com.intel.cedar.engine.xml.loader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * This class is an extension of the JAXP Source interface. The class can be
 * used wherever a JAXP Source object can be used, and it provides additional
 * information about the way that the Source is to be processed: for example, it
 * indicates whether or not it should be validated against a schema. Other
 * options that can be set include the SAX XMLReader to be used, and the choice
 * of whether a source in the form of an existing tree should be copied or
 * wrapped.
 * 
 * @since 8.8
 */

public class ProxySource implements Source {

    private Source source;
    private XMLReader parser = null;
    private int stripSpace;
    private Boolean lineNumbering = null;
    private boolean pleaseClose = false;
    private EntityResolver entityResolver = null;
    private List filters = null;

    /**
     * Create an ProxySource that wraps a given Source object (which must not
     * itself be an ProxySource)
     * 
     * @param source
     *            the Source object to be wrapped. This must be an
     *            implementation of Source that Saxon recognizes, or an
     *            implementation for which a {@link SourceResolver} has been
     *            registered with the {@link Configuration}. The source must not
     *            itself be an ProxySource.
     *            <p>
     *            As an alternative to this constructor, consider using the
     *            factory method {@link #makeProxySource}, which does accept any
     *            kind of Source including an ProxySource as input.
     *            </p>
     * @throws IllegalArgumentException
     *             if the wrapped source is an ProxySource
     * @since 8.8
     */

    private ProxySource(Source source) {
        if (source instanceof ProxySource) {
            throw new IllegalArgumentException(
                    "Contained source must not be an ProxySource");
        }
        this.source = source;
    }

    /**
     * Create an ProxySource that wraps a given Source object. If this is
     * already an ProxySource, the original ProxySource is returned. Note that
     * this means that setting any properties on the returned ProxySource will
     * also affect the original.
     * 
     * @param source
     *            the Source object to be wrapped
     * @return an ProxySource
     * @since 8.8
     */

    public static ProxySource makeProxySource(Source source) {
        if (source instanceof ProxySource) {
            return (ProxySource) source;
        }
        return new ProxySource(source);
    }

    /**
     * Add a filter to the list of filters to be applied to the raw input
     * 
     * @param filter
     *            the filter to be added
     */

    public void addFilter(ProxyReceiver filter) {
        if (filters == null) {
            filters = new ArrayList(5);
        }
        filters.add(filter);
    }

    /**
     * Get the list of filters to be applied to the input. Returns null if there
     * are no filters.
     * 
     * @return the list of filters, if there are any
     */

    public List getFilters() {
        return filters;
    }

    /**
     * Get the Source object wrapped by this ProxySource
     * 
     * @return the contained Source object
     * @since 8.8
     */

    public Source getContainedSource() {
        return source;
    }

    /**
     * Set the space-stripping action to be applied to the source document
     * 
     * @param stripAction
     *            one of {@link net.sf.saxon.value.Whitespace#IGNORABLE},
     *            {@link net.sf.saxon.value.Whitespace#ALL}, or
     *            {@link net.sf.saxon.value.Whitespace#NONE}
     * @since 8.8
     */

    public void setStripSpace(int stripAction) {
        stripSpace = stripAction;
    }

    /**
     * Get the space-stripping action to be applied to the source document
     * 
     * @return one of {@link net.sf.saxon.value.Whitespace#IGNORABLE},
     *         {@link net.sf.saxon.value.Whitespace#ALL}, or
     *         {@link net.sf.saxon.value.Whitespace#NONE}
     * @since 8.8
     */

    public int getStripSpace() {
        return stripSpace;
    }

    /**
     * Set whether line numbers are to be maintained in the constructed document
     * 
     * @param lineNumbering
     *            true if line numbers are to be maintained
     * @since 8.8
     */

    public void setLineNumbering(boolean lineNumbering) {
        this.lineNumbering = Boolean.valueOf(lineNumbering);
    }

    /**
     * Get whether line numbers are to be maintained in the constructed document
     * 
     * @return true if line numbers are maintained
     * @since 8.8
     */

    public boolean isLineNumbering() {
        return lineNumbering != null && lineNumbering.booleanValue();
    }

    /**
     * Determine whether setLineNumbering() has been called
     * 
     * @return true if setLineNumbering() has been called
     * @since 8.9
     */

    public boolean isLineNumberingSet() {
        return lineNumbering != null;
    }

    /**
     * Set the SAX parser (XMLReader) to be used
     * 
     * @param parser
     *            the SAX parser
     * @since 8.8
     */

    public void setXMLReader(XMLReader parser) {
        this.parser = parser;
        if (source instanceof SAXSource) {
            ((SAXSource) source).setXMLReader(parser);
        }
    }

    /**
     * Get the SAX parser (XMLReader) to be used
     * 
     * @return the parser
     * @since 8.8
     */

    public XMLReader getXMLReader() {
        if (parser != null) {
            return parser;
        } else if (source instanceof SAXSource) {
            return ((SAXSource) source).getXMLReader();
        } else {
            return null;
        }
    }

    /**
     * Set the System ID. This sets the System Id on the underlying Source
     * object.
     * 
     * @param id
     *            the System ID. This provides a base URI for the document, and
     *            also the result of the document-uri() function
     * @since 8.8
     */

    public void setSystemId(String id) {
        source.setSystemId(id);
    }

    /**
     * Get the System ID. This gets the System Id on the underlying Source
     * object.
     * 
     * @return the System ID: effectively the base URI.
     * @since 8.8
     */

    public String getSystemId() {
        return source.getSystemId();
    }

    /**
     * Set an EntityResolver to be used when parsing
     * 
     * @param resolver
     *            the EntityResolver to be used
     * @since 8.9
     */

    public void setEntityResolver(EntityResolver resolver) {
        entityResolver = resolver;
    }

    /**
     * Get the EntityResolver that will be used when parsing
     * 
     * @return the EntityResolver, if one has been set using
     *         {@link #setEntityResolver}, otherwise null.
     * @since 8.9
     */

    public EntityResolver getEntityResolver() {
        return entityResolver;
    }

    /**
     * Set whether or not the user of this Source is encouraged to close it as
     * soon as reading is finished. Normally the expectation is that any Stream
     * in a StreamSource will be closed by the component that created the
     * Stream. However, in the case of a Source returned by a URIResolver, there
     * is no suitable interface (the URIResolver has no opportunity to close the
     * stream). Also, in some cases such as reading of stylesheet modules, it is
     * possible to close the stream long before control is returned to the
     * caller who supplied it. This tends to make a difference on .NET, where a
     * file often can't be opened if there is a stream attached to it.
     * 
     * @param close
     *            true if the source should be closed as soon as it has been
     *            consumed
     * @since 8.8
     */

    public void setPleaseCloseAfterUse(boolean close) {
        pleaseClose = close;
    }

    /**
     * Determine whether or not the user of this Source is encouraged to close
     * it as soon as reading is finished.
     * 
     * @return true if the source should be closed as soon as it has been
     *         consumed
     * @since 8.8
     */

    public boolean isPleaseCloseAfterUse() {
        return pleaseClose;
    }

    /**
     * Close any resources held by this Source. This only works if the
     * underlying Source is one that is recognized as holding closable
     * resources.
     * 
     * @since 8.8
     */

    public void close() {
        try {
            if (source instanceof StreamSource) {
                StreamSource ss = (StreamSource) source;
                if (ss.getInputStream() != null) {
                    ss.getInputStream().close();
                }
                if (ss.getReader() != null) {
                    ss.getReader().close();
                }
            } else if (source instanceof SAXSource) {
                InputSource is = ((SAXSource) source).getInputSource();
                if (is != null) {
                    if (is.getByteStream() != null) {
                        is.getByteStream().close();
                    }
                    if (is.getCharacterStream() != null) {
                        is.getCharacterStream().close();
                    }
                }
            }
        } catch (IOException err) {
            // no action
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