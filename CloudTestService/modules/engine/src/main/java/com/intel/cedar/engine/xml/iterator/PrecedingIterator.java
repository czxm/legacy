package com.intel.cedar.engine.xml.iterator;

import com.intel.cedar.engine.xml.model.NodeImpl;

public final class PrecedingIterator extends TreeIterator {

    NodeImpl nextAncestor;

    public PrecedingIterator(NodeImpl node, NodeTest nodeTest) {
        super(node, nodeTest);

        // we need to avoid returning ancestors of the starting node
        nextAncestor = (NodeImpl) node.getParentNode();
        advance();
    }

    /**
     * Special code to skip the ancestors of the start node
     */

    protected boolean conforms(NodeImpl node) {
        // ASSERT: we'll never test the root node, because it's always
        // an ancestor, so nextAncestor will never be null.
        if (node != null) {
            if (node.isSameNode(nextAncestor)) {
                nextAncestor = (NodeImpl) nextAncestor.getParentNode();
                return false;
            }
        }
        return super.conforms(node);
    }

    protected void step() {
        next = next.getPreviousInDocument();
    }

    /**
     * Get another Iterator of the same nodes
     */

    public SequenceIterator getAnother() {
        return new PrecedingIterator(start, nodeTest);
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
