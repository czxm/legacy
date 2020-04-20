package com.intel.ca360.loadmeter.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
This class "pretty prints" an XML stream to something more human-readable.
It duplicates the character content with some modifications to whitespace, 
restoring line breaks and a simple pattern of indenting child elements.

This version of the class acts as a SAX 2.0 <code>DefaultHandler</code>,
so to provide the unformatted XML just pass a new instance to a SAX parser.
Its output is via the {@link #toString toString} method.

One major limitation:  we gather character data for elements in a single
buffer, so mixed-content documents will lose a lot of data!  This works
best with data-centric documents where elements either have single values
or child elements, but not both.

@author Will Provost
*/
/*
Copyright 2002-2003 by Will Provost.
All rights reserved.
*/
public class PrettyPrinter
    extends DefaultHandler
{
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(PrettyPrinter.class);
	
	public static void main(String args[]) {
		String inputFile = null;
		if (args.length == 1) {
			inputFile = args[0];
			
			try {
				File input = new File(inputFile);
				FileInputStream is = new FileInputStream(input);
				
				String prettyOutput = PrettyPrinter.prettyPrint(is);
				System.out.println(prettyOutput);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Usage: inputfile");
		}
	}
    /**
    Convenience method to wrap pretty-printing SAX pass over existing content.
    */
    public static String prettyPrint (byte[] content)
    {
        try
        {
            PrettyPrinter pretty = new PrettyPrinter ();
            SAXParserFactory factory = SAXParserFactory.newInstance ();
            factory.setFeature
                ("http://xml.org/sax/features/namespace-prefixes", true);
            factory.newSAXParser ().parse 
                (new ByteArrayInputStream (content), pretty);
            return pretty.toString ();
        }
        catch (Exception ex)
        {
        	LOG.error("prettyPrint error!", ex);
            return "EXCEPTION: " + ex.getClass ().getName () + " saying \"" +
                ex.getMessage () + "\"";
        }
    }
    
    /**
    Convenience method to wrap pretty-printing SAX pass over existing content.
    */
    public static String prettyPrint (String content)
    {
        try
        {
            PrettyPrinter pretty = new PrettyPrinter ();
            SAXParserFactory factory = SAXParserFactory.newInstance ();
            factory.setFeature
                ("http://xml.org/sax/features/namespace-prefixes", true);
            factory.newSAXParser ().parse (content, pretty);
            return pretty.toString ();
        }
        catch (Exception ex)
        {
        	LOG.error("prettyPrint error!", ex);
            return "EXCEPTION: " + ex.getClass ().getName () + " saying \"" +
                ex.getMessage () + "\"";
        }
    }
    
    /**
    Convenience method to wrap pretty-printing SAX pass over existing content.
    */
    public static String prettyPrint (InputStream content)
    {
        try
        {
            PrettyPrinter pretty = new PrettyPrinter ();
            SAXParserFactory factory = SAXParserFactory.newInstance ();
            factory.setFeature
                ("http://xml.org/sax/features/namespace-prefixes", true);
            factory.newSAXParser ().parse (content, pretty);
            return pretty.toString ();
        }
        catch (Exception ex)
        {
        	LOG.error("prettyPrint error!", ex);
            return "EXCEPTION: " + ex.getClass ().getName () + " saying \"" +
                ex.getMessage () + "\"";
        }
    }

    /**
    Convenience method to wrap pretty-printing SAX pass over existing content.
    */
    public static String prettyPrint (Document doc)
        throws TransformerException
    {
        try
        {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream ();
            TransformerFactory.newInstance ().newTransformer()
                .transform (new DOMSource (doc), new StreamResult (buffer));
            byte[] rawResult = buffer.toByteArray ();
            buffer.close ();
            
            return prettyPrint (rawResult);
        }
        catch (Exception ex)
        {
        	LOG.error("prettyPrint error!", ex);
            return "EXCEPTION: " + ex.getClass ().getName () + " saying \"" +
                ex.getMessage () + "\"";
        }
    }
    
    public static class StreamAdapter
        extends OutputStream
    {
        public StreamAdapter (Writer finalDestination)
        {
            this.finalDestination = finalDestination;
        }
        
        public void write (int b)
        {
            out.write (b);
        }
        
        public void flushPretty ()
            throws IOException
        {
            PrintWriter finalPrinter = new PrintWriter (finalDestination);
            finalPrinter.println 
                (PrettyPrinter.prettyPrint (out.toByteArray ()));
            finalPrinter.close ();
            out.close ();
        }
        
        private ByteArrayOutputStream out = new ByteArrayOutputStream ();
        Writer finalDestination;
    }
    
    /**
    Call this to get the formatted XML post-parsing.
    */
    public String toString ()
    {
        return output.toString ();
    }
    
    /**
    Prints the XML declaration.
    */
    public void startDocument () 
        throws SAXException 
    {
        output.append ("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>")
              .append (endLine);
    }
    
    /**
    Prints a blank line at the end of the reformatted document.
    */
    public void endDocument () throws SAXException 
    {
        output.append (endLine);
    }

    /**
    Writes the start tag for the element.
    Attributes are written out, one to a text line.  Starts gathering
    character data for the element.
    */
    public void startElement 
            (String URI, String name, String qName, Attributes attributes) 
        throws SAXException 
    {
        if (justHitStartTag)
            output.append ('>');

        output.append (endLine)
              .append (indent)
              .append ('<')
              .append (qName);

        int length = attributes.getLength ();        
        for (int a = 0; a < length; ++a) {
        	String attrName = escapeAttrValue(attributes.getQName (a).toCharArray(),
        			0,
        			attributes.getQName (a).toCharArray().length);
        	String value = escapeAttrValue(attributes.getValue (a).toCharArray(),
        			0,
        			attributes.getValue (a).toCharArray().length);
            output.append (standardIndent)
                  .append (attrName)
                  .append ("=\"")
                  .append (value)
                  .append ('\"');
        }
            
        indent += standardIndent;
        currentValue = new StringBuffer ();
        justHitStartTag = true;
    }
    
    /**
    Checks the {@link #currentValue} buffer to gather element content.
    Writes this out if it is available.  Writes the element end tag.
    */
    public void endElement (String URI, String name, String qName) 
        throws SAXException 
    {
        indent = indent.substring 
            (0, indent.length () - standardIndent.length ());
        
        if (currentValue == null)
            output.append (endLine)
                  .append (indent)
                  .append ("</")
                  .append (qName)
                  .append ('>');
        else if (currentValue.length () != 0)
            output.append ('>')
                  .append (currentValue.toString ())
                  .append ("</")
                  .append (qName)
                  .append ('>');
        else
            output.append ("/>");
              
        currentValue = null;
        justHitStartTag = false;
    }
        
    /**
    When the {@link #currentValue} buffer is enabled, appends character
    data into it, to be gathered when the element end tag is encountered.
    */
    public void characters (char[] chars, int start, int length) 
        throws SAXException 
    {
        if (currentValue != null)
            currentValue.append (escapeElmtValue (chars, start, length));
    }

    /**
    Filter to pass strings to output, escaping <, >, and &
    characters to &lt;, &gt; and &amp; respectively.
    */
    private static String escapeElmtValue (char[] chars, int start, int length)
    {
        StringBuffer result = new StringBuffer ();
        for (int c = start; c < start + length; ++c)
            if (chars[c] == '<') {
                result.append ("&lt;");
            } else if (chars[c] == '>') {
            	result.append("&gt;");
            } else if (chars[c] == '&') {
            	if ((c + 3 < start + length && (chars[c + 1] == 'l' && chars[c + 2] == 't' && chars[c + 3] == ';')) ||
                	(c + 3 < start + length && (chars[c + 1] == 'g' && chars[c + 2] == 't' && chars[c + 3] == ';')) ||
                	(c + 4 < start + length && (chars[c + 1] == 'a' && chars[c + 2] == 'm' && chars[c + 3] == 'p' && chars[c + 4] == ';'))) {
                	result.append (chars[c]);
                } else {
                	result.append("&amp;");
                }
            } else {
                result.append (chars[c]);
            }
                
        return result.toString ();
    }
    
    /**
     * Escape attribute value
     * @param chars
     * @param start
     * @param length
     * @return
     */
    private static String escapeAttrValue (char[] chars, int start, int length)
    {
        StringBuffer result = new StringBuffer ();
        for (int c = start; c < start + length; ++c)
            if (chars[c] == '"') {
            	result.append("&quot;");
            } else if (chars[c] == '&') {
            	if ((c + 3 < start + length && (chars[c + 1] == 'l' && chars[c + 2] == 't' && chars[c + 3] == ';')) ||
            		(c + 5 < start + length && (chars[c + 1] == 'q' && chars[c + 2] == 'u' && chars[c + 3] == 'o' && chars[c + 4] == 't') && chars[c + 5] == ';') ||
                	(c + 4 < start + length && (chars[c + 1] == 'a' && chars[c + 2] == 'm' && chars[c + 3] == 'p' && chars[c + 4] == ';'))) {
                	result.append (chars[c]);
                } else {
                	result.append("&amp;");
                }
            } else {
                result.append (chars[c]);
            }
                
        return result.toString ();
    }
    /**
    This whitespace string is expanded and collapsed to manage the output
    indenting.
    */
    private String indent = "";

    /**
    A buffer for character data.  It is &quot;enabled&quot; in 
    {@link #startElement startElement} by being initialized to a 
    new <b>StringBuffer</b>, and then read and reset to 
    <code>null</code> in {@link #endElement endElement}.
    */
    private StringBuffer currentValue = null;

    /**
    The primary buffer for accumulating the formatted XML.
    */
    private StringBuffer output = new StringBuffer ();    
    
    private boolean justHitStartTag;
    
    private static final String standardIndent = "  ";
    private static final String endLine = 
        System.getProperty ("line.separator");
}

