package com.intel.cedar.engine.xml.iterator;

import com.intel.cedar.engine.util.IntHashSet;
import com.intel.cedar.engine.xml.NamespaceConstant;
import com.intel.cedar.engine.xml.model.Node;

/**
 * This class provides an iterator over the namespace codes representing the
 * in-scope namespaces of any node. It relies on nodes to implement the method
 * {@link net.sf.saxon.om.Node#getDeclaredNamespaces(int[])}.
 * 
 * <p>
 * The result does not include the XML namespace.
 * </p>
 */
public class NamespaceCodeIterator implements IntIterator {

    private Node element;
    private int index;
    private int next;
    private int[] localDeclarations;
    IntHashSet undeclared;

    /**
     * Factory method: create an iterator over the in-scope namespace codes for
     * an element
     * 
     * @param element
     *            the element (or other node) whose in-scope namespaces are
     *            required. If this is not an element, the result will be an
     *            empty iterator
     * @return an iterator over the namespace codes. A namespace code is an
     *         integer that represents a prefix-uri binding; the prefix and URI
     *         can be obtained by reference to the name pool. This iterator will
     *         represent all the in-scope namespaces, without duplicates, and
     *         respecting namespace undeclarations. It does not include the XML
     *         namespace.
     */

    public static IntIterator iterateNamespaces(Node element) {
        if (element.getNodeKind() == Node.ELEMENT) {
            return new NamespaceCodeIterator(element);
        } else {
            return EmptyIntIterator.getInstance();
        }
    }

    private NamespaceCodeIterator(Node element) {
        this.element = element;
        undeclared = new IntHashSet(8);
        index = 0;
        localDeclarations = element.getDeclaredNamespaces(null);
    }

    public boolean hasNext() {
        if (next == -1) {
            return false;
        }
        advance();
        return next != -1;
    }

    public int next() {
        return next;
    }

    private void advance() {
        while (true) {
            boolean ascend = index >= localDeclarations.length;
            int nsCode = 0;
            if (!ascend) {
                nsCode = localDeclarations[index++];
                ascend = nsCode == -1;
            }
            if (ascend) {
                element = element.getParentNode();
                if (element != null && element.getNodeKind() == Node.ELEMENT) {
                    localDeclarations = element
                            .getDeclaredNamespaces(localDeclarations);
                    index = 0;
                    continue;
                } else {
                    next = -1;
                    return;
                }
            }
            short uriCode = (short) (nsCode & 0xffff);
            short prefixCode = (short) (nsCode >> 16);
            if (uriCode == 0) {
                // this is an undeclaration
                undeclared.add(prefixCode);
            } else {
                if (undeclared.add(prefixCode)) {
                    // it was added, so it's new, so return it
                    next = nsCode;
                    return;
                }
                // else it wasn't added, so we've already seen it
            }
        }
    }

    /**
     * Get a list of in-scope namespace codes. If an array of namespace codes is
     * needed, without actually constructing the namespace nodes, a caller may
     * create the NamespaceCodeIterator and then call this method. The result is
     * an array of integers, each containing a prefix code in the top half and a
     * uri code in the bottom half. Note that calling this method is
     * destructive: the iterator is consumed and cannot be used again.
     * 
     * @param element
     *            the element whose nodes are required
     * @return the list of in scope namespaces
     */

    public static int[] getInScopeNamespaceCodes(Node element) {
        boolean first = true;
        IntHashSet declared = null;
        IntHashSet undeclared = null;
        int[] buffer = new int[8];
        Node node = element;

        while (node != null && node.getNodeKind() == Node.ELEMENT) {

            int[] nslist = node.getDeclaredNamespaces(buffer);
            if (nslist != null) {
                for (int i = 0; i < nslist.length; i++) {
                    if (nslist[i] == -1) {
                        break;
                    }
                    if (first) {
                        undeclared = new IntHashSet(8);
                        declared = new IntHashSet(8);
                        declared.add(NamespaceConstant.XML_NAMESPACE_CODE);
                        first = false;
                    }
                    short uriCode = (short) (nslist[i] & 0xffff);
                    short prefixCode = (short) (nslist[i] >> 16);
                    if (uriCode == 0) {
                        // this is an undeclaration
                        undeclared.add(prefixCode);
                    } else {
                        if (!undeclared.contains(prefixCode)) {
                            declared.add(nslist[i]);
                            undeclared.add(prefixCode);
                        }
                    }
                }
            }
            node = node.getParentNode();
        }
        if (first) {
            return XML_NAMESPACE_CODE_ARRAY;
        } else {

            int[] codes = new int[declared.size()];
            int i = 0;
            IntIterator ii = declared.iterator();
            while (ii.hasNext()) {
                codes[i++] = ii.next();
            }
            return codes;
        }
    }

    private static int[] XML_NAMESPACE_CODE_ARRAY = { NamespaceConstant.XML_NAMESPACE_CODE };

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
// The Original Code is: all this file
//
// The Initial Developer of the Original Code is Michael H. Kay.
//
// Contributor(s):
//

