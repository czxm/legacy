package com.intel.cedar.engine.xml;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.InputSource;

/**
 * This class provides the service of converting a URI into an InputSource. It
 * is used to get stylesheet modules referenced by xsl:import and xsl:include,
 * and source documents referenced by the document() function. The standard
 * version handles anything that the java URL class will handle. You can write a
 * subclass to handle other kinds of URI, e.g. references to things in a
 * database.
 * 
 * @author Michael H. Kay
 */

public class StandardURIResolver implements URIResolver, Serializable {

    private Configuration config = null;

    /**
     * Create a StandardURIResolver, with no reference to a Configuration. This
     * constructor is not used internally by Saxon, but it may be used by
     * user-written application code. It is deprecated because the
     * StandardURIResolver works best when the Configuration is known.
     * 
     * @deprecated since 8.7
     */

    public StandardURIResolver() {
        this(null);
    }

    /**
     * Create a StandardURIResolver, with a reference to a Configuration
     * 
     * @param config
     *            The Configuration object. This is used to get a SAX Parser for
     *            a source XML document
     */

    public StandardURIResolver(Configuration config) {
        this.config = config;
    }

    /**
     * Set the configuration
     * 
     * @param config
     *            the configuration
     */

    public void setConfiguration(Configuration config) {
        this.config = config;
    }

    /**
     * Get the configuration if available
     * 
     * @return the configuration
     */

    public Configuration getConfiguration() {
        return config;
    }

    /**
     * Resolve a URI
     * 
     * @param href
     *            The relative or absolute URI. May be an empty string. May
     *            contain a fragment identifier starting with "#", which must be
     *            the value of an ID attribute in the referenced XML document.
     * @param base
     *            The base URI that should be used. May be null if uri is
     *            absolute.
     * @return a Source object representing an XML document
     */

    public Source resolve(String href, String base) throws TransformerException {

        // System.err.println("StandardURIResolver, href=" + href + ", base=" +
        // base);

        String relativeURI = href;
        String id = null;

        // Extract any fragment identifier. Note, this code is no longer used to
        // resolve fragment identifiers in URI references passed to the
        // document()
        // function: the code of the document() function handles these itself.

        int hash = href.indexOf('#');
        if (hash >= 0) {
            relativeURI = href.substring(0, hash);
            id = href.substring(hash + 1);
            // System.err.println("StandardURIResolver, href=" + href + ", id="
            // + id);
        }

        URI uri;
        try {
            uri = makeAbsolute(relativeURI, base);
        } catch (URISyntaxException err) {
            throw new TransformerException("Invalid URI " + relativeURI
                    + " - base " + base, err);
        }

        // Check that any "%" sign in the URI is part of a well-formed
        // percent-encoded UTF-8 character.
        // Without this check, dereferencing the resulting URL can fail with
        // arbitrary unchecked exceptions

        final String uriString = uri.toString();

        Source source = new SAXSource();
        setSAXInputSource((SAXSource) source, uriString);

        if (((SAXSource) source).getXMLReader() == null) {
            if (config == null) {
                try {
                    ((SAXSource) source).setXMLReader(SAXParserFactory
                            .newInstance().newSAXParser().getXMLReader());
                } catch (Exception err) {
                    throw new TransformerException(err);
                }
            } else {
                // ((SAXSource)source).setXMLReader(config.getSourceParser());
                // Leave the Sender to allocate an XMLReader, so that it can be
                // returned to the pool after use
            }
        }

        return source;
    }

    /**
     * Construct an absolute URI from a relative URI and a base URI. The method
     * uses the resolve method of the java.net.URI class, except where the base
     * URI uses the (non-standard) "jar:" scheme, in which case the method used
     * is <code>new URL(baseURL, relativeURL)</code>.
     * 
     * <p>
     * Spaces in either URI are converted to %20
     * </p>
     * 
     * <p>
     * If no base URI is available, and the relative URI is not an absolute URI,
     * then the current directory is used as a base URI.
     * </p>
     * 
     * @param relativeURI
     *            the relative URI. Null is permitted provided that the base URI
     *            is an absolute URI
     * @param base
     *            the base URI
     * @return the absolutized URI
     * @throws java.net.URISyntaxException
     *             if either of the strings is not a valid URI or if the
     *             resolution fails
     */

    public URI makeAbsolute(String relativeURI, String base)
            throws URISyntaxException {
        URI absoluteURI;
        // System.err.println("makeAbsolute " + relativeURI + " against base " +
        // base);
        if (relativeURI == null) {
            absoluteURI = new URI(base);
            if (!absoluteURI.isAbsolute()) {
                throw new URISyntaxException(base,
                        "Relative URI not supplied, so base URI must be absolute");
            } else {
                return absoluteURI;
            }
        }

        try {
            if (base == null) {
                absoluteURI = new URI(relativeURI);
            } else {
                URI baseURI = new URI(base);
                new URI(relativeURI); // does validation only
                absoluteURI = (relativeURI.length() == 0 ? baseURI : baseURI
                        .resolve(relativeURI));
            }
        } catch (IllegalArgumentException err0) {
            // can be thrown by resolve() when given a bad URI
            throw new URISyntaxException(relativeURI,
                    "Cannot resolve URI against base " + base);
        }

        return absoluteURI;
    }

    /**
     * Set the InputSource part of the returned SAXSource. This is done in a
     * separate method to allow subclassing. The default implementation simply
     * places the URI in the InputSource, allowing the XML parser to take
     * responsibility for the dereferencing. A subclass may choose to
     * dereference the URI at this point an place an InputStream in the
     * SAXSource.
     * 
     * @param source
     *            the SAXSource being initialized
     * @param uriString
     *            the absolute (resolved) URI to be used
     */

    protected void setSAXInputSource(SAXSource source, String uriString) {
        source.setInputSource(new InputSource(uriString));
        source.setSystemId(uriString);
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
