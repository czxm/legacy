package com.intel.cedar.engine.xml.type;

import java.io.Serializable;

import com.intel.cedar.engine.xml.NamePool;
import com.intel.cedar.engine.xml.NamespaceConstant;
import com.intel.cedar.engine.xml.StandardNames;
import com.intel.cedar.engine.xml.StructuredQName;
import com.intel.cedar.engine.xml.XMLException;
import com.intel.cedar.engine.xml.util.Whitespace;

/**
 * This class represents a built-in atomic type, which may be either a primitive
 * type (such as xs:decimal or xs:anyURI) or a derived type (such as xs:ID or
 * xs:dayTimeDuration).
 */

public class BuiltInAtomicType implements AtomicType, Serializable {

    int fingerprint;
    int baseFingerprint;
    int primitiveFingerprint;
    boolean ordered = false;

    public static BuiltInAtomicType ANY_ATOMIC;

    public static BuiltInAtomicType NUMERIC;

    public static BuiltInAtomicType STRING;

    public static BuiltInAtomicType BOOLEAN;

    public static BuiltInAtomicType DURATION;

    public static BuiltInAtomicType DATE_TIME;

    public static BuiltInAtomicType DATE;

    public static BuiltInAtomicType TIME;

    public static BuiltInAtomicType G_YEAR_MONTH;

    public static BuiltInAtomicType G_MONTH;

    public static BuiltInAtomicType G_MONTH_DAY;

    public static BuiltInAtomicType G_YEAR;

    public static BuiltInAtomicType G_DAY;

    public static BuiltInAtomicType HEX_BINARY;

    public static BuiltInAtomicType BASE64_BINARY;

    public static BuiltInAtomicType ANY_URI;

    public static BuiltInAtomicType QNAME;

    public static BuiltInAtomicType NOTATION;

    public static BuiltInAtomicType UNTYPED_ATOMIC;

    public static BuiltInAtomicType DECIMAL;

    public static BuiltInAtomicType FLOAT;

    public static BuiltInAtomicType DOUBLE;

    public static BuiltInAtomicType INTEGER;

    public static BuiltInAtomicType NON_POSITIVE_INTEGER;

    public static BuiltInAtomicType NEGATIVE_INTEGER;

    public static BuiltInAtomicType LONG;

    public static BuiltInAtomicType INT;

    public static BuiltInAtomicType SHORT;

    public static BuiltInAtomicType BYTE;

    public static BuiltInAtomicType NON_NEGATIVE_INTEGER;

    public static BuiltInAtomicType POSITIVE_INTEGER;

    public static BuiltInAtomicType UNSIGNED_LONG;

    public static BuiltInAtomicType UNSIGNED_INT;

    public static BuiltInAtomicType UNSIGNED_SHORT;

    public static BuiltInAtomicType UNSIGNED_BYTE;

    public static BuiltInAtomicType YEAR_MONTH_DURATION;

    public static BuiltInAtomicType DAY_TIME_DURATION;

    public static BuiltInAtomicType NORMALIZED_STRING;

    public static BuiltInAtomicType TOKEN;

    public static BuiltInAtomicType LANGUAGE;

    public static BuiltInAtomicType NAME;

    public static BuiltInAtomicType NMTOKEN;

    public static BuiltInAtomicType NCNAME;

    public static BuiltInAtomicType ID;

    public static BuiltInAtomicType IDREF;

    public static BuiltInAtomicType ENTITY;

    /**
     * Static initialization
     */

    static {
        ANY_ATOMIC = makeAtomicType(StandardNames.XS_ANY_ATOMIC_TYPE,
                AnySimpleType.getInstance());

        NUMERIC = makeAtomicType(StandardNames.XS_NUMERIC, ANY_ATOMIC);
        NUMERIC.ordered = true;
        STRING = makeAtomicType(StandardNames.XS_STRING, ANY_ATOMIC);
        STRING.ordered = true;
        BOOLEAN = makeAtomicType(StandardNames.XS_BOOLEAN, ANY_ATOMIC);
        BOOLEAN.ordered = true;
        DURATION = makeAtomicType(StandardNames.XS_DURATION, ANY_ATOMIC);
        DATE_TIME = makeAtomicType(StandardNames.XS_DATE_TIME, ANY_ATOMIC);
        DATE_TIME.ordered = true;
        DATE = makeAtomicType(StandardNames.XS_DATE, ANY_ATOMIC);
        DATE.ordered = true;
        TIME = makeAtomicType(StandardNames.XS_TIME, ANY_ATOMIC);
        TIME.ordered = true;
        G_YEAR_MONTH = makeAtomicType(StandardNames.XS_G_YEAR_MONTH, ANY_ATOMIC);
        G_MONTH = makeAtomicType(StandardNames.XS_G_MONTH, ANY_ATOMIC);
        G_MONTH_DAY = makeAtomicType(StandardNames.XS_G_MONTH_DAY, ANY_ATOMIC);
        G_YEAR = makeAtomicType(StandardNames.XS_G_YEAR, ANY_ATOMIC);
        G_DAY = makeAtomicType(StandardNames.XS_G_DAY, ANY_ATOMIC);
        HEX_BINARY = makeAtomicType(StandardNames.XS_HEX_BINARY, ANY_ATOMIC);
        BASE64_BINARY = makeAtomicType(StandardNames.XS_BASE64_BINARY,
                ANY_ATOMIC);
        ANY_URI = makeAtomicType(StandardNames.XS_ANY_URI, ANY_ATOMIC);
        ANY_URI.ordered = true;
        QNAME = makeAtomicType(StandardNames.XS_QNAME, ANY_ATOMIC);
        NOTATION = makeAtomicType(StandardNames.XS_NOTATION, ANY_ATOMIC);
        UNTYPED_ATOMIC = makeAtomicType(StandardNames.XS_UNTYPED_ATOMIC,
                ANY_ATOMIC);
        UNTYPED_ATOMIC.ordered = true;
        DECIMAL = makeAtomicType(StandardNames.XS_DECIMAL, NUMERIC);
        FLOAT = makeAtomicType(StandardNames.XS_FLOAT, NUMERIC);
        DOUBLE = makeAtomicType(StandardNames.XS_DOUBLE, NUMERIC);
        INTEGER = makeAtomicType(StandardNames.XS_INTEGER, DECIMAL);
        NON_POSITIVE_INTEGER = makeAtomicType(
                StandardNames.XS_NON_POSITIVE_INTEGER, INTEGER);
        NEGATIVE_INTEGER = makeAtomicType(StandardNames.XS_NEGATIVE_INTEGER,
                NON_POSITIVE_INTEGER);
        LONG = makeAtomicType(StandardNames.XS_LONG, INTEGER);
        INT = makeAtomicType(StandardNames.XS_INT, LONG);
        SHORT = makeAtomicType(StandardNames.XS_SHORT, INT);
        BYTE = makeAtomicType(StandardNames.XS_BYTE, SHORT);
        NON_NEGATIVE_INTEGER = makeAtomicType(
                StandardNames.XS_NON_NEGATIVE_INTEGER, INTEGER);
        POSITIVE_INTEGER = makeAtomicType(StandardNames.XS_POSITIVE_INTEGER,
                NON_NEGATIVE_INTEGER);
        UNSIGNED_LONG = makeAtomicType(StandardNames.XS_UNSIGNED_LONG,
                NON_NEGATIVE_INTEGER);
        UNSIGNED_INT = makeAtomicType(StandardNames.XS_UNSIGNED_INT,
                UNSIGNED_LONG);
        UNSIGNED_SHORT = makeAtomicType(StandardNames.XS_UNSIGNED_SHORT,
                UNSIGNED_INT);
        UNSIGNED_BYTE = makeAtomicType(StandardNames.XS_UNSIGNED_BYTE,
                UNSIGNED_SHORT);
        YEAR_MONTH_DURATION = makeAtomicType(
                StandardNames.XS_YEAR_MONTH_DURATION, DURATION);
        YEAR_MONTH_DURATION.ordered = true;
        DAY_TIME_DURATION = makeAtomicType(StandardNames.XS_DAY_TIME_DURATION,
                DURATION);
        DAY_TIME_DURATION.ordered = true;
        NORMALIZED_STRING = makeAtomicType(StandardNames.XS_NORMALIZED_STRING,
                STRING);
        TOKEN = makeAtomicType(StandardNames.XS_TOKEN, NORMALIZED_STRING);
        LANGUAGE = makeAtomicType(StandardNames.XS_LANGUAGE, TOKEN);
        NAME = makeAtomicType(StandardNames.XS_NAME, TOKEN);
        NMTOKEN = makeAtomicType(StandardNames.XS_NMTOKEN, TOKEN);
        NCNAME = makeAtomicType(StandardNames.XS_NCNAME, NAME);
        ID = makeAtomicType(StandardNames.XS_ID, NCNAME);
        IDREF = makeAtomicType(StandardNames.XS_IDREF, NCNAME);
        ENTITY = makeAtomicType(StandardNames.XS_ENTITY, NCNAME);

        ANY_ATOMIC.ordered = true; // give it the benefit of the doubt: used
                                   // only for static types

    }

    private BuiltInAtomicType(int fingerprint) {
        this.fingerprint = fingerprint;
    }

    /**
     * Get the local name of this type
     * 
     * @return the local name of this type definition, if it has one. Return
     *         null in the case of an anonymous type.
     */

    public String getName() {
        if (fingerprint == StandardNames.XS_NUMERIC) {
            return "numeric";
        } else {
            return StandardNames.getLocalName(fingerprint);
        }
    }

    /**
     * Get the target namespace of this type
     * 
     * @return the target namespace of this type definition, if it has one.
     *         Return null in the case of an anonymous type, and in the case of
     *         a global type defined in a no-namespace schema.
     */

    public String getTargetNamespace() {
        return NamespaceConstant.SCHEMA;
    }

    /**
     * Determine whether the type is abstract, that is, whether it cannot have
     * instances that are not also instances of some concrete subtype
     */

    public boolean isAbstract() {
        switch (fingerprint) {
        case StandardNames.XS_NOTATION:
        case StandardNames.XS_ANY_ATOMIC_TYPE:
        case StandardNames.XS_NUMERIC:
        case StandardNames.XS_ANY_SIMPLE_TYPE:
            return true;
        default:
            return false;
        }
    }

    /**
     * Return true if this is an external object type, that is, a Saxon-defined
     * type for external Java or .NET objects
     */

    public boolean isExternalType() {
        return false;
    }

    /**
     * Determine whether this is a built-in type or a user-defined type
     */

    public boolean isBuiltInType() {
        return true;
    }

    /**
     * Determine whether the atomic type is ordered, that is, whether less-than
     * and greater-than comparisons are permitted
     * 
     * @return true if ordering operations are permitted
     */

    public boolean isOrdered() {
        return ordered;
    }

    /**
     * Get the URI of the schema document where the type was originally defined.
     * 
     * @return the URI of the schema document. Returns null if the information
     *         is unknown or if this is a built-in type
     */

    public String getSystemId() {
        return null; // AUTO
    }

    /**
     * Determine whether the atomic type is numeric
     * 
     * @return true if the type is a built-in numeric type
     */

    public boolean isPrimitiveNumeric() {
        switch (fingerprint) {
        case StandardNames.XS_INTEGER:
        case StandardNames.XS_DECIMAL:
        case StandardNames.XS_DOUBLE:
        case StandardNames.XS_FLOAT:
        case StandardNames.XS_NUMERIC:
            return true;
        default:
            return false;
        }
    }

    /**
     * Get the most specific possible atomic type that all items in this
     * SimpleType belong to
     * 
     * @return the lowest common supertype of all member types
     */

    public AtomicType getCommonAtomicType() {
        return this;
    }

    /**
     * Get the validation status - always valid
     */
    public final int getValidationStatus() {
        return VALIDATED;
    }

    /**
     * Returns the value of the 'block' attribute for this type, as a
     * bit-significant integer with fields such as
     * {@link SchemaType#DERIVATION_LIST} and
     * {@link SchemaType#DERIVATION_EXTENSION}
     * 
     * @return the value of the 'block' attribute for this type
     */

    public final int getBlock() {
        return 0;
    }

    /**
     * Gets the integer code of the derivation method used to derive this type
     * from its parent. Returns zero for primitive types.
     * 
     * @return a numeric code representing the derivation method, for example
     *         {@link SchemaType#DERIVATION_RESTRICTION}
     */

    public final int getDerivationMethod() {
        return SchemaType.DERIVATION_RESTRICTION;
    }

    /**
     * Determines whether derivation (of a particular kind) from this type is
     * allowed, based on the "final" property
     * 
     * @param derivation
     *            the kind of derivation, for example
     *            {@link SchemaType#DERIVATION_LIST}
     * @return true if this kind of derivation is allowed
     */

    public final boolean allowsDerivation(int derivation) {
        return true;
    }

    /**
     * Set the base type of this type
     * 
     * @param baseFingerprint
     *            the namepool fingerprint of the name of the base type
     */

    public final void setBaseTypeFingerprint(int baseFingerprint) {
        this.baseFingerprint = baseFingerprint;
    }

    /**
     * Get the fingerprint of the name of this type
     * 
     * @return the fingerprint. Returns an invented fingerprint for an anonymous
     *         type.
     */

    public int getFingerprint() {
        return fingerprint;
    }

    /**
     * Get the namecode of the name of this type. This includes the prefix from
     * the original type declaration: in the case of built-in types, there may
     * be a conventional prefix or there may be no prefix.
     */

    public int getNameCode() {
        return fingerprint;
    }

    /**
     * Get the name of the type as a QName
     * 
     * @return a StructuredQName containing the name of the type. The
     *         conventional prefix "xs" is used to represent the XML Schema
     *         namespace
     */

    public StructuredQName getQualifiedName() {
        return new StructuredQName("xs", NamespaceConstant.SCHEMA,
                StandardNames.getLocalName(fingerprint));
    }

    /**
     * Get the display name of the type: that is, a lexical QName with an
     * arbitrary prefix
     * 
     * @return a lexical QName identifying the type
     */

    public String getDisplayName() {
        if (fingerprint == StandardNames.XS_NUMERIC) {
            return "numeric";
        } else {
            return StandardNames.getDisplayName(fingerprint);
        }
    }

    /**
     * Determine whether the atomic type is a primitive type. The primitive
     * types are the 19 primitive types of XML Schema, plus xs:integer,
     * xs:dayTimeDuration and xs:yearMonthDuration; xs:untypedAtomic; and all
     * supertypes of these (xs:anyAtomicType, xs:numeric, ...)
     * 
     * @return true if the type is considered primitive under the above rules
     */

    public boolean isPrimitiveType() {
        return Type.isPrimitiveType(fingerprint);
    }

    /**
     * Test whether this SchemaType is a complex type
     * 
     * @return true if this SchemaType is a complex type
     */

    public final boolean isComplexType() {
        return false;
    }

    /**
     * Test whether this is an anonymous type
     * 
     * @return true if this SchemaType is an anonymous type
     */

    public boolean isAnonymousType() {
        return false;
    }

    /**
     * Returns the base type that this type inherits from. This method can be
     * used to get the base type of a type that is known to be valid. If this
     * type is a Simpletype that is a built in primitive type then null is
     * returned.
     * 
     * @return the base type.
     * @throws IllegalStateException
     *             if this type is not valid.
     */

    public final SchemaType getBaseType() {
        if (baseFingerprint == -1) {
            return null;
        } else {
            return BuiltInType.getSchemaType(baseFingerprint);
        }
    }

    /**
     * Get the type from which this item type is derived by restriction. This is
     * the supertype in the XPath type heirarchy, as distinct from the Schema
     * base type: this means that the supertype of xs:boolean is
     * xs:anyAtomicType, whose supertype is item() (rather than
     * xs:anySimpleType).
     * 
     * @param th
     *            the type hierarchy cache, not used in this implementation
     * @return the supertype, or null if this type is item()
     */

    public ItemType getSuperType(TypeHierarchy th) {
        SchemaType base = getBaseType();
        if (base instanceof AnySimpleType) {
            return AnyItemType.getInstance();
        } else {
            return (ItemType) base;
        }
    }

    /**
     * Get the primitive item type corresponding to this item type. For item(),
     * this is Type.ITEM. For node(), it is Type.NODE. For specific node kinds,
     * it is the value representing the node kind, for example Type.ELEMENT. For
     * anyAtomicValue it is Type.ATOMIC_VALUE. For numeric it is Type.NUMBER.
     * For other atomic types it is the primitive type as defined in XML Schema,
     * except that INTEGER is considered to be a primitive type.
     */

    public ItemType getPrimitiveItemType() {
        if (isPrimitiveType()) {
            return this;
        } else {
            ItemType s = (ItemType) getBaseType();
            if (s.isAtomicType()) {
                return s.getPrimitiveItemType();
            } else {
                return this;
            }
        }
    }

    /**
     * Get the primitive type corresponding to this item type. For item(), this
     * is Type.ITEM. For node(), it is Type.NODE. For specific node kinds, it is
     * the value representing the node kind, for example Type.ELEMENT. For
     * anyAtomicValue it is Type.ATOMIC_VALUE. For numeric it is Type.NUMBER.
     * For other atomic types it is the primitive type as defined in XML Schema,
     * except that INTEGER is considered to be a primitive type.
     */

    public int getPrimitiveType() {
        return primitiveFingerprint;
        // int x = getFingerprint();
        // if (isPrimitiveType()) {
        // return x;
        // } else {
        // SchemaType s = getBaseType();
        // if (s.isAtomicType()) {
        // return ((AtomicType)s).getPrimitiveType();
        // } else {
        // return getFingerprint();
        // }
        // }
    }

    /**
     * Determine whether this type is supported in a basic XSLT processor
     * 
     * @return true if this type is permitted in a basic XSLT processor
     */

    public boolean isAllowedInBasicXSLT() {
        return (isPrimitiveType() && getFingerprint() != StandardNames.XS_NOTATION);
    }

    /**
     * Produce a representation of this type name for use in error messages.
     * Where this is a QName, it will use conventional prefixes
     */

    public String toString(NamePool pool) {
        return getDisplayName();
    }

    /**
     * Get the item type of the atomic values that will be produced when an item
     * of this type is atomized
     */

    public AtomicType getAtomizedItemType() {
        return this;
    }

    /**
     * Returns the base type that this type inherits from. This method can be
     * used to get the base type of a type that is known to be valid. If this
     * type is a Simpletype that is a built in primitive type then null is
     * returned.
     * 
     * @return the base type.
     * @throws IllegalStateException
     *             if this type is not valid.
     */

    public SchemaType getKnownBaseType() {
        return getBaseType();
    }

    /**
     * Test whether this is the same type as another type. They are considered
     * to be the same type if they are derived from the same type definition in
     * the original XML representation (which can happen when there are multiple
     * includes of the same file)
     */

    public boolean isSameType(SchemaType other) {
        return other.getFingerprint() == getFingerprint();
    }

    public String getDescription() {
        return getDisplayName();
    }

    public String toString() {
        return getDisplayName();
    }

    /**
     * Check that this type is validly derived from a given type
     * 
     * @param type
     *            the type from which this type is derived
     * @param block
     *            the derivations that are blocked by the relevant element
     *            declaration
     * @throws XMLException
     *             if the derivation is not allowed
     */

    public void checkTypeDerivationIsOK(SchemaType type, int block)
            throws XMLException {
        if (type == AnySimpleType.getInstance()) {
            // OK
        } else if (isSameType(type)) {
            // OK
        } else {
            SchemaType base = getBaseType();
            if (base == null) {
                throw new XMLException("Type " + getDescription()
                        + " is not validly derived from "
                        + type.getDescription());
            }
            try {
                base.checkTypeDerivationIsOK(type, block);
            } catch (XMLException se) {
                throw new XMLException("Type " + getDescription()
                        + " is not validly derived from "
                        + type.getDescription());
            }
        }
    }

    /**
     * Returns true if this SchemaType is a SimpleType
     * 
     * @return true (always)
     */

    public final boolean isSimpleType() {
        return true;
    }

    /**
     * Test whether this Simple Type is an atomic type
     * 
     * @return true, this is an atomic type
     */

    public boolean isAtomicType() {
        return true;
    }

    /**
     * Returns true if this type is derived by list, or if it is derived by
     * restriction from a list type, or if it is a union that contains a list as
     * one of its members
     * 
     * @return true if this is a list type
     */

    public boolean isListType() {
        return false;
    }

    /**
     * Return true if this type is a union type (that is, if its variety is
     * union)
     * 
     * @return true for a union type
     */

    public boolean isUnionType() {
        return false;
    }

    /**
     * Determine the whitespace normalization required for values of this type
     * 
     * @param th
     *            the type hierarchy cache
     * @return one of PRESERVE, REPLACE, COLLAPSE
     */

    public int getWhitespaceAction(TypeHierarchy th) {
        switch (getFingerprint()) {
        case StandardNames.XS_STRING:
            return Whitespace.PRESERVE;
        case StandardNames.XS_NORMALIZED_STRING:
            return Whitespace.REPLACE;
        default:
            return Whitespace.COLLAPSE;
        }
    }

    /**
     * Returns the built-in base type this type is derived from.
     * 
     * @return the first built-in type found when searching up the type
     *         hierarchy
     */
    public SchemaType getBuiltInBaseType() {
        BuiltInAtomicType base = this;
        while ((base != null) && (base.getFingerprint() > 1023)) {
            base = (BuiltInAtomicType) base.getBaseType();
        }
        return base;
    }

    /**
     * Test whether this simple type is namespace-sensitive, that is, whether it
     * is derived from xs:QName or xs:NOTATION
     * 
     * @return true if this type is derived from xs:QName or xs:NOTATION
     */

    public boolean isNamespaceSensitive() {
        BuiltInAtomicType base = this;
        int fp = base.getFingerprint();
        while (fp > 1023) {
            base = (BuiltInAtomicType) base.getBaseType();
            fp = base.getFingerprint();
        }

        return fp == StandardNames.XS_QNAME || fp == StandardNames.XS_NOTATION;
    }

    /**
     * Two types are equal if they have the same fingerprint. Note: it is
     * normally safe to use ==, because we always use the static constants, one
     * instance for each built in atomic type. However, after serialization and
     * deserialization a different instance can appear.
     */

    public boolean equals(Object obj) {
        return obj instanceof BuiltInAtomicType
                && getFingerprint() == ((BuiltInAtomicType) obj)
                        .getFingerprint();
    }

    /**
     * The fingerprint can be used as a hashcode
     */

    public int hashCode() {
        return getFingerprint();
    }

    /**
     * Internal factory method to create a BuiltInAtomicType. There is one
     * instance for each of the built-in atomic types
     * 
     * @param fingerprint
     *            The name of the type
     * @param baseType
     *            The base type from which this type is derived
     * @return the newly constructed built in atomic type
     */
    private static BuiltInAtomicType makeAtomicType(int fingerprint,
            SimpleType baseType) {
        BuiltInAtomicType t = new BuiltInAtomicType(fingerprint);
        t.setBaseTypeFingerprint(baseType.getFingerprint());
        if (t.isPrimitiveType()) {
            t.primitiveFingerprint = fingerprint;
        } else {
            t.primitiveFingerprint = ((AtomicType) baseType).getPrimitiveType();
        }
        if (baseType instanceof BuiltInAtomicType) {
            t.ordered = ((BuiltInAtomicType) baseType).ordered;
        }
        BuiltInType.register(t.getFingerprint(), t);
        return t;
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
// The Initial Developer of the Original Code is Saxonica Limited
//
// Portions created by (your name) are Copyright (C) (your legal entity). All
// Rights Reserved.
//
// Contributor(s): none
//