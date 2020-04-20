package com.intel.cedar.engine.xml.iterator;

import com.intel.cedar.engine.xml.model.DocumentImpl;
import com.intel.cedar.engine.xml.model.Node;
import com.intel.cedar.engine.xml.model.NodeImpl;

public final class FollowingIterator extends TreeIterator {

    private NodeImpl root;

    public FollowingIterator(NodeImpl node, NodeTest nodeTest) {
        super(node, nodeTest);
        root = (DocumentImpl) node.getOwnerDocument();
        // skip the descendant nodes if any
        int type = node.getNodeKind();
        if (type == Node.ATTRIBUTE || type == Node.NAMESPACE) {
            next = ((NodeImpl) node.getParentNode()).getNextInDocument(root);
        } else {
            do {
                next = (NodeImpl) node.getNextSibling();
                if (next == null)
                    node = (NodeImpl) node.getParentNode();
            } while (next == null && node != null);
        }
        while (!conforms(next)) {
            step();
        }
    }

    protected void step() {
        next = next.getNextInDocument(root);
    }

    /**
     * Get another Iterator of the same nodes
     */

    public SequenceIterator getAnother() {
        return new FollowingIterator(start, nodeTest);
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
