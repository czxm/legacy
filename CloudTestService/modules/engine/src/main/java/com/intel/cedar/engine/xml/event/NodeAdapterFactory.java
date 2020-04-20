/*******************************************************************************
 * Copyright (c) 2001, 2006 IBM Corporation and others.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * An adapter factory to create JFaceNodeAdapters. Use this adapter factory with
 * a JFaceAdapterContentProvider to display DOM nodes in a tree.
 */
public class NodeAdapterFactory extends AbstractAdapterFactory implements
        INodeAdapterFactory {
    /**
     * This keeps track of all the listeners.
     */
    private Set fListeners = new HashSet();

    protected INodeAdapter singletonAdapter;

    public NodeAdapterFactory() {
        this(NodeAdapter.class, true);
    }

    public NodeAdapterFactory(Object adapterKey, boolean registerAdapters) {
        super(adapterKey, registerAdapters);
    }

    public synchronized void addListener(Object listener) {
        fListeners.add(listener);
    }

    public INodeAdapterFactory copy() {
        return new NodeAdapterFactory(getAdapterKey(),
                isShouldRegisterAdapter());
    }

    /**
     * Create a new JFace adapter for the DOM node passed in
     */
    protected INodeAdapter createAdapter(INodeNotifier node) {
        if (singletonAdapter == null) {
            // create the JFaceNodeAdapter
            singletonAdapter = new NodeAdapter(this);
            initAdapter(singletonAdapter, node);
        }
        return singletonAdapter;
    }

    /**
     * returns "copy" so no one can modify our list. It is a shallow copy.
     */
    public synchronized Collection getListeners() {
        return new ArrayList(fListeners);
    }

    protected void initAdapter(INodeAdapter adapter, INodeNotifier node) {
    }

    public void release() {
        fListeners.clear();
        if (singletonAdapter != null && singletonAdapter instanceof NodeAdapter) {
            // do nothing here
        }
    }

    public synchronized void removeListener(Object listener) {
        fListeners.remove(listener);
    }
}
