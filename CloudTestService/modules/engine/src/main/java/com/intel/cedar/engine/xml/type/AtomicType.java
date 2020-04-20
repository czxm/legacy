package com.intel.cedar.engine.xml.type;

/**
 * Interface for atomic types (these are either built-in atomic types or
 * user-defined atomic types). An AtomicType is both an ItemType (a possible
 * type for items in a sequence) and a SchemaType (a possible type for
 * validating and annotating nodes).
 */
public interface AtomicType extends SimpleType, ItemType {

    /**
     * Determine whether the atomic type is ordered, that is, whether less-than
     * and greater-than comparisons are permitted
     * 
     * @return true if ordering operations are permitted
     */

    public boolean isOrdered();

    /**
     * Determine whether the type is abstract, that is, whether it cannot have
     * instances that are not also instances of some concrete subtype
     */

    public boolean isAbstract();

    /**
     * Determine whether the atomic type is a primitive type. The primitive
     * types are the 19 primitive types of XML Schema, plus xs:integer,
     * xs:dayTimeDuration and xs:yearMonthDuration; xs:untypedAtomic; and all
     * supertypes of these (xs:anyAtomicType, xs:numeric, ...)
     * 
     * @return true if the type is considered primitive under the above rules
     */

    public boolean isPrimitiveType();

    /**
     * Determine whether the atomic type is a built-in type. The built-in atomic
     * types are the 41 atomic types defined in XML Schema, plus
     * xs:dayTimeDuration and xs:yearMonthDuration, xs:untypedAtomic, and all
     * supertypes of these (xs:anyAtomicType, xs:numeric, ...)
     */

    public boolean isBuiltInType();
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

