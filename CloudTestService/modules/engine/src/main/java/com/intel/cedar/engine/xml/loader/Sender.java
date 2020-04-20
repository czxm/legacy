package com.intel.cedar.engine.xml.loader;

import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import com.intel.cedar.engine.xml.Configuration;
import com.intel.cedar.engine.xml.XMLException;
import com.intel.cedar.engine.xml.util.Whitespace;

/*
 import net.sf.saxon.AugmentedSource;
 import net.sf.saxon.Configuration;
 import net.sf.saxon.StandardErrorHandler;
 import net.sf.saxon.Controller;
 import net.sf.saxon.om.*;
 import net.sf.saxon.pull.PullProvider;
 import net.sf.saxon.pull.PullPushCopier;
 import net.sf.saxon.pull.PullSource;
 import net.sf.saxon.trans.XSLException;
 import net.sf.saxon.type.Type;
 import net.sf.saxon.type.SchemaType;
 import net.sf.saxon.value.Whitespace;



 */

/**
 * Sender is a helper class that sends events to a Receiver from any kind of
 * Source object
 */

public class Sender {

    ReceiverConfiguration recevierConfiguration;

    /**
     * Create a Sender
     * 
     * @param pipe
     *            the pipeline configuration
     */
    public Sender(ReceiverConfiguration recevierConfiguration) {
        this.recevierConfiguration = recevierConfiguration;
    }

    /**
     * Send the contents of a Source to a Receiver. Note that if the Source
     * identifies an element node rather than a document node, only the subtree
     * rooted at that element will be copied.
     * 
     * @param source
     *            the document or element to be copied
     * @param receiver
     *            the destination to which it is to be copied
     */

    public void send(Source source, Receiver receiver) throws XMLException {
        send(source, receiver, false);
    }

    /**
     * Send the contents of a Source to a Receiver. Note that if the Source
     * identifies an element node rather than a document node, only the subtree
     * rooted at that element will be copied.
     * 
     * @param source
     *            the document or element to be copied
     * @param receiver
     *            the destination to which it is to be copied
     * @param isFinal
     *            set to true when the document is being processed purely for
     *            the sake of validation, in which case multiple validation
     *            errors in the source can be reported.
     */

    public void send(Source source, Receiver receiver, boolean isFinal)
            throws XMLException {
        Configuration config = recevierConfiguration.getConfiguration();
        receiver.setReceiverConfiguration(recevierConfiguration);
        receiver.setSystemId(source.getSystemId());
        Receiver next = receiver;

        int stripSpace = Whitespace.UNSPECIFIED;

        XMLReader parser = null;

        if (source instanceof ProxySource) {
            parser = ((ProxySource) source).getXMLReader();

            List filters = ((ProxySource) source).getFilters();
            if (filters != null) {
                for (int i = filters.size() - 1; i >= 0; i--) {
                    ProxyReceiver filter = (ProxyReceiver) filters.get(i);
                    filter.setReceiverConfiguration(recevierConfiguration);
                    filter.setSystemId(source.getSystemId());
                    filter.setUnderlyingReceiver(next);
                    next = filter;
                }
            }

            source = ((ProxySource) source).getContainedSource();
        }

        if (source instanceof SAXSource) {
            sendSAXSource((SAXSource) source, next, stripSpace);
        } else if (source instanceof StreamSource) {
            StreamSource ss = (StreamSource) source;

            String url = source.getSystemId();
            InputSource is = new InputSource(url);
            is.setCharacterStream(ss.getReader());
            is.setByteStream(ss.getInputStream());
            boolean reuseParser = false;
            if (parser == null) {
                parser = config.getSourceParser();
                reuseParser = true;
            }
            SAXSource sax = new SAXSource(parser, is);
            sax.setSystemId(source.getSystemId());
            sendSAXSource(sax, next, stripSpace);
            if (reuseParser) {
                config.reuseSourceParser(parser);
            }
        } else {
            throw new IllegalArgumentException(
                    "Sender can only handle source of type "
                            + source.getClass().getName());
        }
    }

    private void sendSAXSource(SAXSource source, Receiver receiver,
            int stripSpace) throws XMLException {
        XMLReader parser = source.getXMLReader();
        boolean reuseParser = false;
        final Configuration config = recevierConfiguration.getConfiguration();
        if (parser == null) {
            SAXSource ss = new SAXSource();
            ss.setInputSource(source.getInputSource());
            ss.setSystemId(source.getSystemId());
            parser = config.getSourceParser();
            ss.setXMLReader(parser);
            source = ss;
            reuseParser = true;
        } else {
            // user-supplied parser: ensure that it meets the namespace
            // requirements
            Configuration.configureParser(parser);
        }

        parser.setErrorHandler(new StandardErrorHandler());

        ReceiverContentHandler ce;
        final ContentHandler ch = parser.getContentHandler();
        if (ch instanceof ReceiverContentHandler) {
            ce = (ReceiverContentHandler) ch;
            ce.reset();
        } else {
            ce = new ReceiverContentHandler();
            parser.setContentHandler(ce);
            parser.setDTDHandler(ce);
            try {
                parser.setProperty(
                        "http://xml.org/sax/properties/lexical-handler", ce);
            } catch (SAXNotSupportedException err) { // this just means we won't
                                                     // see the comments
                throw new UnsupportedOperationException(err.getMessage());
            } catch (SAXNotRecognizedException err) {
                throw new UnsupportedOperationException(err.getMessage());
            }
        }
        ce.setReceiver(receiver);
        ce.setReceiverConfiguration(recevierConfiguration);
        try {
            parser.parse(source.getInputSource());
        } catch (SAXException err) {
            Exception nested = err.getException();
            if (nested instanceof XMLException) {
                throw (XMLException) nested;
            } else if (nested instanceof RuntimeException) {
                throw (RuntimeException) nested;
            } else {
                XMLException de = new XMLException(err);
                throw de;
            }
        } catch (java.io.IOException err) {
            throw new XMLException(err);
        }
        if (reuseParser) {
            config.reuseSourceParser(parser);
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
// The Initial Developer of the Original Code is Michael H. Kay
//
// Portions created by (your name) are Copyright (C) (your legal entity). All
// Rights Reserved.
//
// Contributor(s): none.
//
