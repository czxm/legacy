package com.intel.cedar.engine.xml.type;

/**
 * This interface represents a simple type, which may be a built-in simple type,
 * or a user-defined simple type.
 */

public interface SimpleType extends SchemaType {

    /**
     * Test whether this Simple Type is an atomic type
     * 
     * @return true if this is an atomic type
     */

    boolean isAtomicType();

    /**
     * Test whether this Simple Type is a list type
     * 
     * @return true if this is a list type
     */
    boolean isListType();

    /**
     * Test whether this Simple Type is a union type
     * 
     * @return true if this is a union type
     */

    boolean isUnionType();

    /**
     * Return true if this is an external object type, that is, a Saxon-defined
     * type for external Java or .NET objects
     * 
     * @return true if this is an external type
     */

    boolean isExternalType();

    /**
     * Get the most specific possible atomic type that all items in this
     * SimpleType belong to
     * 
     * @return the lowest common supertype of all member types
     */

    AtomicType getCommonAtomicType();

    /**
     * Determine whether this is a built-in type or a user-defined type
     * 
     * @return true if this is a built-in type
     */

    boolean isBuiltInType();

    /**
     * Get the built-in type from which this type is derived by restriction
     * 
     * @return the built-in type from which this type is derived by restriction.
     *         This will not necessarily be a primitive type.
     */

    SchemaType getBuiltInBaseType();

    /**
     * Test whether this type is namespace sensitive, that is, if a namespace
     * context is needed to translate between the lexical space and the value
     * space. This is true for types derived from, or containing, QNames and
     * NOTATIONs
     * 
     * @return true if the type is namespace-sensitive
     */

    boolean isNamespaceSensitive();

    /**
     * Determine how values of this simple type are whitespace-normalized.
     * 
     * @return one of {@link net.sf.saxon.value.Whitespace#PRESERVE},
     *         {@link net.sf.saxon.value.Whitespace#COLLAPSE},
     *         {@link net.sf.saxon.value.Whitespace#REPLACE}.
     * @param th
     *            the type hierarchy cache
     */

    public int getWhitespaceAction(TypeHierarchy th);
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

