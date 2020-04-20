package com.intel.cedar.engine.xml.loader;

import java.io.PrintWriter;
import java.io.Writer;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class StandardErrorHandler implements org.xml.sax.ErrorHandler {

    // //////////////////////////////////////////////////////////////////////////
    // Implement the org.xml.sax.ErrorHandler interface.
    // //////////////////////////////////////////////////////////////////////////

    private Writer errorOutput;

    public StandardErrorHandler() {
    }

    /**
     * Set output for error messages produced by the default error handler. The
     * default error handler does not throw an exception for parse errors or
     * input I/O errors, rather it returns a result code and writes diagnostics
     * to a user-specified output writer, which defaults to System.err<BR>
     * This call has no effect if setErrorHandler() has been called to supply a
     * user-defined error handler
     * 
     * @param writer
     *            The Writer to use for error messages
     */

    public void setErrorOutput(Writer writer) {
        errorOutput = writer;
    }

    /**
     * Callback interface for SAX: not for application use
     */

    public void warning(SAXParseException e) {
        // ignore
    }

    /**
     * Callback interface for SAX: not for application use
     */

    public void error(SAXParseException e) throws SAXException {
        // System.err.println("ErrorHandler.error " + e.getMessage());
        reportError(e, false);
    }

    /**
     * Callback interface for SAX: not for application use
     */

    public void fatalError(SAXParseException e) throws SAXException {
        // System.err.println("ErrorHandler.fatalError " + e.getMessage());
        reportError(e, true);
        throw e;
    }

    /**
     * Common routine for SAX errors and fatal errors
     */

    protected void reportError(SAXParseException e, boolean isFatal) {
        try {
            if (errorOutput == null) {
                errorOutput = new PrintWriter(System.err);
            }
            String errcat = (isFatal ? "Fatal error" : "Error");
            errorOutput.write(errcat + " reported by XML parser: "
                    + e.getMessage() + '\n');
            errorOutput.write("  URL:    " + e.getSystemId() + '\n');
            errorOutput.write("  Line:   " + e.getLineNumber() + '\n');
            errorOutput.write("  Column: " + e.getColumnNumber() + '\n');
            errorOutput.flush();
        } catch (Exception e2) {
            System.err.println(e);
            System.err.println(e2);
            e2.printStackTrace();
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