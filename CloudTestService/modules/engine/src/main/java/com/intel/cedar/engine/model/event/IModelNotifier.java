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

package com.intel.cedar.engine.model.event;

import java.util.Collection;

/**
 * INodeNotifiers and INodeAdapters form a collaboration that allows clients to
 * use the typical adapter pattern but with notification added, that is,
 * client's adapters will be notified when the nodeNotifier changes.
 * 
 * @plannedfor 1.0
 */

public interface IModelNotifier {
    /**
     * Add an adapter of this notifier.
     * 
     * @param adapter
     *            the adapter to be added
     * 
     */
    void addAdapter(IModelAdapter adapter);

    /**
     * Return an exisiting adapter of type "type" or if none found create a new
     * adapter using a registered adapter factory
     */
    IModelAdapter getAdapterFor(Object type);

    /**
     * Return a read-only Collection of the Adapters to this notifier.
     * 
     * @return collection of adapters.
     */
    Collection getAdapters();

    /**
     * Return an exisiting adapter of type "type" or null if none found
     */
    IModelAdapter getExistingAdapter(Object type);

    /**
     * Remove an adapter of this notifier. If the adapter does not exist for
     * this node notifier, this request is ignored.
     * 
     * @param adapter
     *            the adapter to remove
     */
    void removeAdapter(IModelAdapter adapter);

    /**
     * sent to adapter when its nodeNotifier changes.
     */
    void notifyChange(ChangeEvent evt);

}
