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
import com.intel.cedar.engine.xml.model.DocumentImpl;

/**
 * IStructuredModels are mainly interesting by their extensions and
 * implementers. The main purposed of this abstraction is to provide a common
 * means to manage models that have an associated structured document.
 * 
 * @plannedfor 2.0
 * 
 *             <p>
 *             ISSUE: this interface needs ton of cleanup!
 *             </p>
 * 
 *             <p>
 *             This interface is not intended to be implemented by clients.
 *             </p>
 */
public interface IStructuredModel {// extends IAdaptable {

    /**
     * 
     * @return The model's FactoryRegistry. A model is not valid without one.
     */
    AdapterFactoryRegistry getFactoryRegistry();

    /**
     * Returns the DOM Document.
     * 
     * @return the DOM Document.
     */
    DocumentImpl getDocument();

}
