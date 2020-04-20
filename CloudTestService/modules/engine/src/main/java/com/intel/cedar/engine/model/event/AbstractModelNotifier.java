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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * AbstractNotifier is similar to (and based on) the EMF NotifierImpl class, but
 * is not related to EMF per se. This class is simpler (that is, not as many
 * functions).
 * 
 * Implementers of this INodeNotifier must subclass this class.
 */
public abstract class AbstractModelNotifier implements IModelNotifier {
    private final static int growthConstant = 3;
    private int adapterCount = 0;

    private IModelAdapter[] fAdapters;

    /**
     * AbstractNotifier constructor comment.
     */
    public AbstractModelNotifier() {
        super();
    }

    /**
     * addAdapter method comment.
     */
    public synchronized void addAdapter(IModelAdapter adapter) {

        if (adapter == null)
            return;
        ensureCapacity(adapterCount + 1);
        fAdapters[adapterCount++] = adapter;
    }

    private void ensureCapacity(int needed) {
        if (fAdapters == null) {
            // first time
            fAdapters = new IModelAdapter[needed + growthConstant];
            return;
        }
        int oldLength = fAdapters.length;
        if (oldLength < needed) {
            IModelAdapter[] oldAdapters = fAdapters;
            IModelAdapter[] newAdapters = new IModelAdapter[needed
                    + growthConstant];
            System.arraycopy(oldAdapters, 0, newAdapters, 0, adapterCount);
            fAdapters = newAdapters;
        }
    }

    /**
     * NOT API: used only for testing.
     * 
     * @return int
     */
    public int getAdapterCount() {
        return adapterCount;
    }

    /**
     * Default behavior for getting an adapter.
     */
    public IModelAdapter getAdapterFor(Object type) {
        // first, we'll see if we already have one
        IModelAdapter result = getExistingAdapter(type);
        // if we didn't find one in our list already,
        // let's create it
        if (result == null) {
            ModelAdapterFactoryRegistry reg = getModelAdapterFactoryRegistry();
            if (reg != null) {
                IModelAdapterFactory factory = reg.getFactoryFor(type);
                if (factory != null) {
                    result = factory.adapt(this);
                }
            }
            // We won't prevent null from being returned, but it would be
            // unusual.
            // It might be because Factory is not working correctly, or
            // not installed, so we'll allow warning message.
            if ((result == null)) {
                //Warning: no adapter was found or created for " + type); //$NON-NLS-1$
            }
        }
        return result;
    }

    /**
     * Returns a shallow clone of list, since clients should not manipulate our
     * list directly. Instead, they should use add/removeAdapter.
     */
    public Collection getAdapters() {
        if (fAdapters != null) {
            if (adapterCount == 0) {
                fAdapters = null;
                return Collections.EMPTY_LIST;
            } else {
                // we need to make a new array, to be sure
                // it doesn't contain nulls at end, which may be
                // present there for "growth".
                IModelAdapter[] tempAdapters = new IModelAdapter[adapterCount];
                System.arraycopy(fAdapters, 0, tempAdapters, 0, adapterCount);
                // EMF uses the unmodifiableCollection. Its a bit of a
                // performance
                // drain, but may want to leave in since
                // it would "fail fast" if someone was trying to modify the
                // list.
                return Collections.unmodifiableCollection(Arrays
                        .asList(tempAdapters));
                // return Arrays.asList(newAdapters);
            }
        } else
            return Collections.EMPTY_LIST;
    }

    public IModelAdapter getExistingAdapter(Object type) {
        IModelAdapter result = null;
        for (int i = 0; i < adapterCount; i++) {
            IModelAdapter a = fAdapters[i];
            if (a != null && a.isAdapterForType(type)) {
                result = a;
                break;
            }
        }
        // if we didn't find one in our list,
        // return the null result
        return result;
    }

    public void notifyChange(ChangeEvent evt) {

        if (fAdapters != null) {
            int localAdapterCount = 0;
            IModelAdapter[] localAdapters = null;

            // lock object while making local assignments
            synchronized (this) {
                localAdapterCount = adapterCount;
                localAdapters = new IModelAdapter[localAdapterCount];
                System.arraycopy(fAdapters, 0, localAdapters, 0,
                        localAdapterCount);
            }

            for (int i = 0; i < localAdapterCount; i++) {
                IModelAdapter a = localAdapters[i];

                try {
                    // ** keep this line identical with debug version!!
                    a.notifyChange(evt);
                } catch (Exception e) {
                    // Its important to "keep going", since notifications
                    // occur between an
                    // aboutToChange event and a changed event -- the
                    // changed event typically being require
                    // to restore state, etc. So, we just log message, do
                    // not re-throw it, but
                    // typically the exception does indicate a serious
                    // program error.
                    //A structured model client, " + a + " threw following exception during adapter notification (" + INodeNotifier.EVENT_TYPE_STRINGS[eventType] + " )", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }

            }
        }
    }

    public synchronized void removeAdapter(IModelAdapter a) {
        if (fAdapters == null || a == null)
            return;
        int newIndex = 0;
        IModelAdapter[] newAdapters = new IModelAdapter[fAdapters.length];
        int oldAdapterCount = adapterCount;
        boolean found = false;
        for (int oldIndex = 0; oldIndex < oldAdapterCount; oldIndex++) {
            IModelAdapter candidate = fAdapters[oldIndex];
            if (a == candidate) {
                adapterCount--;
                found = true;
            } else
                newAdapters[newIndex++] = fAdapters[oldIndex];
        }
        if (found)
            fAdapters = newAdapters;
    }

    abstract public ModelAdapterFactoryRegistry getModelAdapterFactoryRegistry();
}
