/*******************************************************************************
 * Copyright (c) 2001, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jens Lukowski/Innoopract - initial renaming/restructuring
 *     
 *******************************************************************************/
package com.intel.cedar.engine.xml.document;

import com.intel.cedar.engine.xml.model.Attribute;
import com.intel.cedar.engine.xml.model.Element;
import com.intel.cedar.engine.xml.model.Node;

/**
 * Provides means to get the XMLModel form of IStrucutredModel. Not to be
 * implemented or extended by clients.
 * 
 * @plannedfor 1.0
 */
public interface IDOMModel extends IStructuredModel {

    /**
     * childReplaced method called when child added, removed or replaced
     * 
     */
    void childReplaced(Node parentNode, Node newChild, Node oldChild);

    /**
     * valueChanged method
     * 
     * @param node
     *            org.w3c.dom.Node
     */
    public void valueChanged(Node node);

    /**
     * nameChanged method
     * 
     * @param node
     *            org.w3c.dom.Node
     */
    public void nameChanged(Node node);

    /**
     * namespacesChanged method
     * 
     * @param node
     *            org.w3c.dom.Node
     */
    public void namespaceChanged(Node node);

    /**
     * attrReplaced method
     * 
     * @param element
     *            org.w3c.dom.Element
     * @param newAttr
     *            org.w3c.dom.Attr
     * @param oldAttr
     *            org.w3c.dom.Attr
     */
    public void attrReplaced(Element element, Attribute newAttr,
            Attribute oldAttr);

    /**
     * NOT CLIENT API
     * 
     * Returns an XMLModelNotifier. Clients should not use.
     * 
     * ISSUE: should be "internalized".
     * 
     */
    DOMModelNotifier getModelNotifier();

    /**
     * NOT CLIENT API
     * 
     * Sets the model notifier Clients should not use.
     * 
     * ISSUE: need to review with legacy clients.
     */
    void setModelNotifier(DOMModelNotifier notifier);
}
