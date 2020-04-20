package com.intel.cedar.engine.xml.loader;

import com.intel.cedar.engine.xml.model.AttributeCollectionImpl;
import com.intel.cedar.engine.xml.model.DocumentImpl;
import com.intel.cedar.engine.xml.model.ElementImpl;

//////////////////////////////////////////////////////////////////////////////
// Inner class DefaultNodeFactory. This creates the nodes in the tree.
// It can be overridden, e.g. when building the stylesheet tree
//////////////////////////////////////////////////////////////////////////////

public class DefaultNodeFactory implements NodeFactory {

    public ElementImpl makeElementNode(DocumentImpl ownerDocument,
            int nameCode, AttributeCollectionImpl attlist, int[] namespaces,
            int namespacesUsed, LocationProvider locator, int locationId)

    {
        ElementImpl e;

        e = new ElementImpl();
        if (namespacesUsed > 0) {
            ((ElementImpl) e).setNamespaceDeclarations(namespaces,
                    namespacesUsed);
        }

        int lineNumber = -1;
        if (locator != null)
            lineNumber = locator.getLineNumber(locationId);

        e.initialise(nameCode, attlist, ownerDocument);
        e.setLineNumber(lineNumber);
        return e;
    }
}
