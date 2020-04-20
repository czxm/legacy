package com.intel.cedar.engine.xml.model;

/**
 * A node in the XML parse tree representing character content
 * <P>
 * 
 * @author Michael H. Kay
 */

public final class TextImpl extends CharacterDataImpl implements Text {

    public TextImpl(DocumentImpl ownerDocument, String content) {
        super(ownerDocument, content);
    }

    /**
     * TextImpl constructor
     * 
     * @param that
     *            TextImpl
     */
    protected TextImpl(TextImpl that) {
        super(that);
    }

    /**
     * Return the character value of the node.
     * 
     * @return the string value of the node
     */

    public String getStringValue() {
        return getData();
    }

    /**
     * Return the type of node.
     * 
     * @return Node.TEXT
     */

    public final int getNodeKind() {
        return Node.TEXT;
    }

    /**
     * cloneNode method
     * 
     * @return org.w3c.dom.Node
     * @param deep
     *            boolean
     */
    public Node cloneNode(boolean deep) {
        TextImpl cloned = new TextImpl(this);
        return cloned;
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