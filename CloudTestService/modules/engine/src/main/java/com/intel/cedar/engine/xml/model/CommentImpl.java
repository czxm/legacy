package com.intel.cedar.engine.xml.model;

/**
 * CommentImpl is an implementation of a Comment node
 * 
 * @author Michael H. Kay
 */

public final class CommentImpl extends CharacterDataImpl implements Comment {

    public CommentImpl(DocumentImpl ownerDocument, String content) {
        super(ownerDocument, content);
    }

    /**
     * CommentImpl constructor
     * 
     * @param that
     *            CommentImpl
     */
    protected CommentImpl(CommentImpl that) {
        super(that);
    }

    public final String getStringValue() {
        return getData();
    }

    public final int getNodeKind() {
        return Node.COMMENT;
    }

    /**
     * cloneNode method
     * 
     * @return org.w3c.dom.Node
     * @param deep
     *            boolean
     */
    public Node cloneNode(boolean deep) {
        CommentImpl cloned = new CommentImpl(this);
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
