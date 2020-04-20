/*******************************************************************************
 * Copyright (c) 2001, 2009 IBM Corporation and others.
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
package com.intel.cedar.engine.xml.model;

/**
 * CharacterDataImpl class
 */
public abstract class CharacterDataImpl extends NodeImpl implements
        CharacterData {

    String data;

    /**
     * CharacterDataImpl constructor
     */
    public CharacterDataImpl(DocumentImpl ownerDocument) {
        super(ownerDocument);
    }

    /**
     * CharacterDataImpl constructor
     * 
     * @param that
     *            CharacterDataImpl
     */
    protected CharacterDataImpl(CharacterDataImpl that) {
        super(that);

        if (that != null) {
            this.data = that.data;
        }
    }

    /**
     * CharacterDataImpl constructor
     */
    public CharacterDataImpl(DocumentImpl ownerDocument, String data) {
        super(ownerDocument);
        this.data = data;
    }

    /**
     * appendData method
     * 
     * @param arg
     *            java.lang.String
     */
    public void appendData(String arg) {
        if (arg == null)
            return;

        String data = getData();
        if (data == null)
            data = arg;
        else
            data += arg;
        setData(data);
    }

    /**
     * deleteData method
     * 
     * @param offset
     *            int
     * @param count
     *            int
     */
    public void deleteData(int offset, int count) {
        if (count == 0)
            return;

        if (count < 0 || offset < 0) {
            throw new IllegalArgumentException("index or count is invalid");
        }

        String data = getData();
        if (data == null) {
            throw new IllegalArgumentException("index or count is invalid");
        }
        int length = data.length();
        if (offset > length) {
            throw new IllegalArgumentException("index or count is invalid");
        }
        if (offset == 0) {
            if (count > length) {
                throw new IllegalArgumentException("index or count is invalid");
            }
            if (count == length)
                data = new String();
            else
                data = data.substring(count);
        } else {
            int end = offset + count;
            if (end > length) {
                throw new IllegalArgumentException("index or count is invalid");
            }
            if (end == length)
                data = data.substring(0, offset);
            else
                data = data.substring(0, offset) + data.substring(end);
        }
        setData(data);
    }

    /**
     * getData method
     * 
     * @return java.lang.String
     */
    public String getData() {
        return data;
    }

    /**
     * getLength method
     * 
     * @return int
     */
    public int getLength() {
        String data = getData();
        if (data == null)
            return 0;
        return data.length();
    }

    /**
     * getNodeValue method
     * 
     * @return java.lang.String
     */
    public String getNodeValue() {
        return getData();
    }

    /**
     * insertData method
     * 
     * @param offset
     *            int
     * @param arg
     *            java.lang.String
     */
    public void insertData(int offset, String arg) {
        if (arg == null)
            return;

        if (offset < 0) {
            throw new IllegalArgumentException("offset is invalid");
        }

        String data = getData();
        if (data == null) {
            if (offset > 0) {
                throw new IllegalArgumentException("offset is invalid");
            }
            data = arg;
        } else if (offset == 0) {
            data = arg + data;
        } else {
            int length = data.length();
            if (offset > length) {
                throw new IllegalArgumentException("offset is invalid");
            }
            if (offset == length)
                data += arg;
            else
                data = data.substring(0, offset) + arg + data.substring(offset);
        }
        setData(data);
    }

    /**
     * replaceData method
     * 
     * @param offset
     *            int
     * @param count
     *            int
     * @param arg
     *            java.lang.String
     */
    public void replaceData(int offset, int count, String arg) {

        if (arg == null) {
            deleteData(offset, count);
            return;
        }
        if (count == 0) {
            insertData(offset, arg);
            return;
        }
        if (offset < 0 || count < 0) {
            throw new IllegalArgumentException("offset or count is invalid");
        }

        String data = getData();
        if (data == null) {
            throw new IllegalArgumentException("offset or count is invalid");
        } else if (offset == 0) {
            int length = data.length();
            if (count > length) {
                throw new IllegalArgumentException("offset or count is invalid");
            }
            if (count == length)
                data = arg;
            else
                data = arg + data.substring(count);
        } else {
            int length = data.length();
            int end = offset + count;
            if (end > length) {
                throw new IllegalArgumentException("offset or count is invalid");
            }
            if (end == length)
                data = data.substring(0, offset) + arg;
            else
                data = data.substring(0, offset) + arg + data.substring(end);
        }
        setData(data);
    }

    /**
     * setData method
     * 
     * @param data
     *            java.lang.String
     */
    public void setData(String data) {
        this.data = data;
        notifyValueChanged();
    }

    /**
     * setNodeValue method
     * 
     * @param textvalue_2
     *            java.lang.String
     */
    public void setNodeValue(String nodeValue) {
        setData(nodeValue);
    }

    /**
     * substringData method
     * 
     * @return java.lang.String
     * @param offset
     *            int
     * @param count
     *            int
     */
    public String substringData(int offset, int count) {
        if (count == 0)
            return new String();
        if (offset < 0 || count < 0) {
            throw new IllegalArgumentException("offset or count is invalid");
        }

        String data = getData();
        if (data == null) {
            throw new IllegalArgumentException("offset or count is invalid");
        }
        int length = data.length();
        if (offset == 0 && count == length)
            return data;
        if (offset > length) {
            throw new IllegalArgumentException("offset or count is invalid");
        }
        int end = offset + count;
        if (end > length) {
            throw new IllegalArgumentException("offset or count is invalid");
        }
        return data.substring(offset, end);
    }

}
