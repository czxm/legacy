package com.intel.cedar.engine.xml;

import org.xml.sax.Locator;

/**
 * A NamespaceException represents an error condition whereby a QName (for
 * example a variable name or template name) uses a namespace prefix that is not
 * declared
 */

public class XMLException extends RuntimeException {
    Locator locator;

    public XMLException() {
        super();
    }

    public XMLException(String message) {
        super(message);
    }

    public XMLException(String message, Throwable e) {
        super(message, e);
    }

    public XMLException(Throwable e) {
        super(e);
    }

    public XMLException(String message, Locator locator) {
        super(message);
        this.locator = locator;
    }

    public XMLException(String message, Locator locator, Throwable e) {
        super(message, e);
        this.locator = locator;
    }

    public Locator getLocator() {
        return locator;
    }

    public void setLocator(Locator locator) {
        this.locator = locator;
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
// Contributor(s): none
//