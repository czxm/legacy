/*******************************************************************************
 * Copyright (c) 2001, 2007 IBM Corporation and others.
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
package com.intel.cedar.engine.xml.event;

import java.util.Collection;
import java.util.Iterator;

import com.intel.cedar.engine.xml.model.Node;

public class NodeAdapter implements INodeAdapter {

    final static Class ADAPTER_KEY = NodeAdapter.class;

    NodeAdapterFactory fAdapterFactory;

    public NodeAdapter(NodeAdapterFactory adapterFactory) {
        super();
        this.fAdapterFactory = adapterFactory;
    }

    /**
     * Allowing the INodeAdapter to compare itself against the type allows it to
     * return true in more than one case.
     */
    public boolean isAdapterForType(Object type) {
        if (type == null) {
            return false;
        }
        return type.equals(ADAPTER_KEY);
    }

    /**
     * Called by the object being adapter (the notifier) when something has
     * changed.
     */
    public void notifyChanged(INodeNotifier notifier, int eventType,
            Object changedFeature, Object oldValue, Object newValue, int pos) {
        // future_TODO: the 'uijobs' used in this method were added to solve
        // threading problems when the dom
        // is updated in the background while the editor is open. They may be
        // a bit overkill and not that useful.
        // (That is, may be be worthy of job manager management). If they are
        // found to be important enough to leave in,
        // there's probably some optimization that can be done.
        if (notifier instanceof Node) {
            Collection listeners = fAdapterFactory.getListeners();
            Iterator iterator = listeners.iterator();

            while (iterator.hasNext()) {
                Object listener = iterator.next();

            }
        }
    }
}
