/*******************************************************************************
 * Copyright (c) 2001, 2008 IBM Corporation and others.
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

import com.intel.cedar.engine.xml.event.AdapterFactoryRegistry;

public abstract class AbstractStructuredModel implements IStructuredModel {

    private AdapterFactoryRegistry factoryRegistry;
    private String fBaseLocation;
    boolean fDirty;

    /**
     * AbstractStructuredModel constructor comment.
     */
    public AbstractStructuredModel() {
        super();
    }

    /**
     * @return java.lang.String
     */
    public java.lang.String getBaseLocation() {
        return fBaseLocation;
    }

    /**
	 * 
	 */
    public AdapterFactoryRegistry getFactoryRegistry() {
        if (factoryRegistry == null) {
            factoryRegistry = new AdapterFactoryRegistry();
        }
        return factoryRegistry;
    }

    /**
     * Is dirty
     */
    public boolean isDirty() {
        return fDirty;
    }

    /**
     * Set dirty
     */
    public void setDirty(boolean fDirty) {
        // no need to process (set or fire event), if same value
        if (this.fDirty != fDirty) {
            // pre-change notification

            // the actual change
            this.fDirty = fDirty;
        }
    }

    /**
     * This attribute is typically used to denote the model's underlying
     * resource.
     */
    public void setBaseLocation(java.lang.String newBaseLocation) {
        fBaseLocation = newBaseLocation;
    }

    /**
     * @deprecated - will likely be deprecated soon, in favor of direct 'adds'
     *             ... but takes some redesign.
     */
    public void setFactoryRegistry(AdapterFactoryRegistry factoryRegistry) {
        this.factoryRegistry = factoryRegistry;
    }
}
