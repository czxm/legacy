package com.intel.cedar.engine.xml.model;

/**
 * PIImpl is an implementation of ProcInstInfo used by the Propagator to
 * construct its trees.
 * 
 * @author Michael H. Kay
 */

public class ProcessingInstructionImpl extends NodeImpl implements
        ProcessingInstruction {

    String data;
    int nameCode;

    public ProcessingInstructionImpl(DocumentImpl ownerDocument, int nameCode,
            String content) {
        super(ownerDocument);
        this.nameCode = nameCode;
        this.data = content;
    }

    /**
     * ProcessingInstructionImpl constructor
     * 
     * @param that
     *            ProcessingInstructionImpl
     */
    protected ProcessingInstructionImpl(ProcessingInstructionImpl that) {
        super(that);

        if (that != null) {
            this.nameCode = that.nameCode;
            this.data = that.data;
        }
    }

    /**
     * Get the nameCode of the node. This is used to locate the name in the
     * NamePool
     */

    public int getNameCode() {
        return nameCode;
    }

    public String getStringValue() {
        return data;
    }

    public final int getNodeKind() {
        return Node.PROCESSING_INSTRUCTION;
    }

    // DOM methods

    /**
     * The target of this processing instruction. XML defines this as being the
     * first token following the markup that begins the processing instruction.
     */

    public String getTarget() {
        return getLocalPart();
    }

    /**
     * The content of this processing instruction. This is from the first non
     * white space character after the target to the character immediately
     * preceding the <code>?&gt;</code> .
     */

    public String getData() {
        return data;
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
     * getNodeValue method
     * 
     * @return java.lang.String
     */
    public String getNodeValue() {
        return getData();
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
     * cloneNode method
     * 
     * @return org.w3c.dom.Node
     * @param deep
     *            boolean
     */
    public Node cloneNode(boolean deep) {
        ProcessingInstructionImpl cloned = new ProcessingInstructionImpl(this);
        return cloned;
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
// The Initial Developer of the Original Code is Michael H. Kay.
//
// Portions created by (your name) are Copyright (C) (your legal entity). All
// Rights Reserved.
//
// Contributor(s): none.
//
