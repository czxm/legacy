package com.intel.cedar.engine.xml.loader;

import java.util.Date;

import javax.xml.transform.Source;

import com.intel.cedar.engine.xml.Configuration;
import com.intel.cedar.engine.xml.NamePool;
import com.intel.cedar.engine.xml.XMLException;
import com.intel.cedar.engine.xml.model.Node;

/**
 * The abstract Builder class is responsible for taking a stream of SAX events
 * and constructing a Document tree. There is one concrete subclass for each
 * tree implementation.
 * 
 * @author Michael H. Kay
 */

public abstract class Builder implements Receiver {
    protected ReceiverConfiguration receiverConfiguration;
    protected Configuration config;
    protected NamePool namePool;
    protected String systemId;
    protected String baseURI;
    protected Node currentRoot;

    protected boolean started = false;
    protected boolean timing = false;
    private boolean open = false;

    private long startTime;

    /**
     * create a Builder and initialise variables
     */

    public Builder() {
    }

    public void setReceiverConfiguration(
            ReceiverConfiguration receiverConfiguration) {
        this.receiverConfiguration = receiverConfiguration;
        config = receiverConfiguration.getConfiguration();
        namePool = config.getNamePool();
    }

    public ReceiverConfiguration getReceiverConfiguration() {
        return receiverConfiguration;
    }

    /**
     * Get the Configuration
     * 
     * @return the Saxon configuration
     */

    public Configuration getConfiguration() {
        return config;
    }

    /**
     * The SystemId is equivalent to the document-uri property defined in the
     * XDM data model. It should be set only in the case of a document that is
     * potentially retrievable via this URI. This means it should not be set in
     * the case of a temporary tree constructed in the course of executing a
     * query or transformation.
     * 
     * @param systemId
     *            the SystemId, that is, the document-uri.
     */

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    /**
     * The SystemId is equivalent to the document-uri property defined in the
     * XDM data model. It should be set only in the case of a document that is
     * potentially retrievable via this URI. This means the value will be null
     * in the case of a temporary tree constructed in the course of executing a
     * query or transformation.
     * 
     * @return the SystemId, that is, the document-uri.
     */

    public String getSystemId() {
        return systemId;
    }

    /**
     * Set the base URI of the document node of the tree being constructed by
     * this builder
     * 
     * @param baseURI
     *            the base URI
     */

    public void setBaseURI(String baseURI) {
        this.baseURI = baseURI;
    }

    /**
     * Get the base URI of the document node of the tree being constructed by
     * this builder
     * 
     * @return the base URI
     */

    public String getBaseURI() {
        return baseURI;
    }

    // ///////////////////////////////////////////////////////////////////////
    // Methods setting and getting options for building the tree
    // ///////////////////////////////////////////////////////////////////////

    /**
     * Set timing option on or off
     * 
     * @param on
     *            set to true to turn timing on. This causes the builder to
     *            display statistical information about the tree that is
     *            constructed. It corresponds to the command line -t option
     */

    public void setTiming(boolean on) {
        timing = on;
    }

    /**
     * Get timing option
     * 
     * @return true if timing information has been requested
     */

    public boolean isTiming() {
        return timing;
    }

    public void open() throws XMLException {
        if (timing && !open) {
            System.err.println("Building tree for " + getSystemId() + " using "
                    + getClass());
            startTime = (new Date()).getTime();
        }
        open = true;
    }

    public void close() throws XMLException {
        if (timing && open) {
            long endTime = (new Date()).getTime();
            System.err.println("Tree built in " + (endTime - startTime)
                    + " milliseconds");
            startTime = endTime;
        }
        open = false;
    }

    /**
     * Start of a document node. This event is ignored: we simply add the
     * contained elements to the current document
     */

    public void startDocument(int properties) throws XMLException {
    }

    /**
     * Notify the end of a document node
     */

    public void endDocument() throws XMLException {
    }

    /**
     * Get the current root node. This will normally be a document node, but if
     * the root of the tree is an element node, it can be an element.
     * 
     * @return the root of the tree that is currently being built, or that has
     *         been most recently built using this builder
     */

    public Node getCurrentRoot() {
        return currentRoot;
    }

    /**
     * Static method to build a document from any kind of Source object. If the
     * source is already in the form of a tree, it is wrapped as required.
     * <p>
     * <i>The preferred way to construct a document tree from a Source object is
     * to use the method {@link Configuration#buildDocument}.</i>
     * </p>
     * 
     * @param source
     *            Any javax.xml.transform.Source object
     * @param stripper
     *            A stripper object, if whitespace text nodes are to be
     *            stripped; otherwise null.
     * @param loadConfig
     *            The Configuration object
     * @return the Node of the start node in the resulting document object.
     */

    public static Node build(Source source, Stripper stripper,
            ReceiverConfiguration receiverConfiguration) throws XMLException {
        Configuration config = receiverConfiguration.getConfiguration();
        if (source == null) {
            throw new NullPointerException(
                    "Source supplied to builder cannot be null");
        }

        Node start;

        Builder b = new TreeBuilder();
        b.setReceiverConfiguration(receiverConfiguration);
        Receiver receiver = b;
        if (stripper != null) {
            stripper.setUnderlyingReceiver(b);
            receiver = stripper;
        }
        try {
            new Sender(receiverConfiguration).send(source, receiver);
        } catch (XMLException err) {
            throw err;
        }
        start = b.getCurrentRoot();
        return start;
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
