package com.intel.cedar.engine.xml.loader;

import com.intel.cedar.engine.xml.Configuration;

/**
 * A ReceiverConfiguration sets options that apply to all the operations in a
 * pipeline. Unlike the global Configuration, these options are always local to
 * a process.
 */

public class ReceiverConfiguration {

    private Configuration config;
    private LocationProvider locationProvider;

    /**
     * Create a ReceiverConfiguration. Note: the normal way to create a
     * ReceiverConfiguration is via the factory methods in the Controller and
     * Configuration classes
     */

    public ReceiverConfiguration() {
    }

    /**
     * Create a ReceiverConfiguration as a copy of an existing
     * ReceiverConfiguration
     * 
     * @param p
     *            the existing ReceiverConfiguration
     */

    public ReceiverConfiguration(ReceiverConfiguration p) {
        config = p.config;
        locationProvider = p.locationProvider;
    }

    /**
     * Make a ReceiverConfiguration from the properties of this Configuration
     * 
     * @return a new ReceiverConfiguration
     * @since 8.4
     */

    static public ReceiverConfiguration makeReceiverConfiguration(
            Configuration config) {
        ReceiverConfiguration receiverConfiguration = new ReceiverConfiguration();
        receiverConfiguration.setConfiguration(config);
        return receiverConfiguration;
    }

    /**
     * Get the Saxon Configuration object
     * 
     * @return the Saxon Configuration
     */

    public Configuration getConfiguration() {
        return config;
    }

    /**
     * Set the Saxon Configuration object
     * 
     * @param config
     *            the Saxon Configuration
     */

    public void setConfiguration(Configuration config) {
        this.config = config;
    }

    /**
     * Get the LocationProvider for interpreting location ids passed down this
     * pipeline
     * 
     * @return the appropriate LocationProvider
     */

    public LocationProvider getLocationProvider() {
        return locationProvider;
    }

    /**
     * Set the LocationProvider for interpreting location ids passed down this
     * pipeline
     * 
     * @param locationProvider
     *            the LocationProvider
     */

    public void setLocationProvider(LocationProvider locationProvider) {
        this.locationProvider = locationProvider;
    }
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
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is Michael H. Kay
//
// Portions created by (your name) are Copyright (C) (your legal entity). All
// Rights Reserved.
//
// Contributor(s): none.
//

