/*
 * INTEL CONFIDENTIAL
 * Copyright 2007 Intel Corporation All Rights Reserved. 
 * 
 * The source code contained or described herein and all documents related to the 
 * source code ("Material") are owned by Intel Corporation or its suppliers or 
 * licensors. Title to the Material remains with Intel Corporation or its suppliers 
 * and licensors. The Material contains trade secrets and proprietary and 
 * confidential information of Intel or its suppliers and licensors. The Material 
 * is protected by worldwide copyright and trade secret laws and treaty provisions. 
 * No part of the Material may be used, copied, reproduced, modified, published, 
 * uploaded, posted, transmitted, distributed, or disclosed in any way without 
 * Intel's prior express written permission.
 * 
 * No license under any patent, copyright, trade secret or other intellectual 
 * property right is granted to or conferred upon you by disclosure or delivery of 
 * the Materials, either expressly, by implication, inducement, estoppel or 
 * otherwise. Any license under such intellectual property rights must be express 
 * and approved by Intel in writing.
 */

package com.intel.cedar.mail;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Retrieves localized messages for this package.
 * 
 * @since 5.1
 * @author Wade Poziombka
 */
final public class Messages {

    private static ResourceBundle getResource() {
        final StringBuffer b = new StringBuffer();
        /*
         * b.append(Messages.class.getName()); final int i = b.lastIndexOf(".");
         * b.delete(i + 1, b.length());
         */
        b.append("messages");
        return ResourceBundle.getBundle(b.toString());
    }

    /**
     * Restricted.
     */
    private Messages() {
    }

    /**
     * Returns localized text based on the provided key.
     * 
     * @param key
     *            the key to the text
     * @return the localized text
     */
    public static String getString(final String key) {
        try {
            return getResource().getString(key);
        } catch (final MissingResourceException e) {
            return '!' + key + '!';
        }
    }

    /**
     * Returns formatted localized text based on the provided key. The text is
     * formatted given using the provided arguments.
     * 
     * @param key
     *            the key to the text format in the resource bundle.
     * @param args
     *            the arguments to use to format the text.
     * @return the localized text
     */
    public static String getString(final String key, final Object... args) {
        return new MessageFormat(getString(key)).format(args);
    }
}