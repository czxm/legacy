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
 * 
 * XMLModelNotifier manages the notification process. Clients should not use
 * extend or reference.
 * 
 * ISSUE: should be internalized.
 */

public interface DOMModelNotifier {

    /**
     * attrReplaced method
     * 
     * @param element
     *            org.w3c.dom.Element
     * @param newAttr
     *            org.w3c.dom.Attribute
     * @param oldAttr
     *            org.w3c.dom.Attribute
     */
    void attrReplaced(Element element, Attribute newAttr, Attribute oldAttr);

    /**
     * Signal that changing is starting.
     * 
     */
    void beginChanging();

    /**
     * Signal that changing is starting with a brand new model.
     * 
     */
    void beginChanging(boolean newModel);

    /**
     * Cancel pending notifications. This is called in the context of
     * "reinitialization" so is assumed ALL notifications can be safely
     * canceled, assuming that once factories and adapters are re-initialized
     * they will be re-notified as text is set in model, if still appropriate.
     */
    void cancelPending();

    /**
     * childReplaced method
     * 
     * @param parentNode
     *            org.w3c.dom.Node
     * @param newChild
     *            org.w3c.dom.Node
     * @param oldChild
     *            org.w3c.dom.Node
     */
    void childReplaced(Node parentNode, Node newChild, Node oldChild);

    /**
     * Editable state changed for node.
     * 
     */
    void editableChanged(Node node);

    /**
     * Signal changing is finished.
     * 
     */
    void endChanging();

    /**
     * Signal end tag changed.
     * 
     * @param element
     * 
     */
    void endTagChanged(Element element);

    /**
     * Used to reflect state of model.
     * 
     * @return true if model had changed.
     * 
     */
    boolean hasChanged();

    /**
     * Used to reflect state of parsing process.
     * 
     * @return true if model is currently changing.
     */
    boolean isChanging();

    /**
     * signal property changed
     * 
     * @param node
     */
    void propertyChanged(Node node);

    /**
     * signal start tag changed
     * 
     * @param element
     */
    void startTagChanged(Element element);

    /**
     * signal structured changed.
     * 
     * @param node
     */
    void structureChanged(Node node);

    /**
     * valueChanged method
     * 
     * @param node
     *            org.w3c.dom.Node
     */
    void valueChanged(Node node);

    /**
     * nameChanged method
     * 
     * @param node
     *            org.w3c.dom.Node
     */
    void nameChanged(Node node);

    /**
     * namespaceChanged method
     * 
     * @param node
     *            org.w3c.dom.Node
     */
    void namespaceChanged(Node node);
}
