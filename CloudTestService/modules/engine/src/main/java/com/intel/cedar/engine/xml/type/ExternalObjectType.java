package com.intel.cedar.engine.xml.type;

import com.intel.cedar.engine.xml.Configuration;
import com.intel.cedar.engine.xml.NamePool;
import com.intel.cedar.engine.xml.NamespaceConstant;
import com.intel.cedar.engine.xml.SchemaException;
import com.intel.cedar.engine.xml.StandardNames;
import com.intel.cedar.engine.xml.XMLException;
import com.intel.cedar.engine.xml.util.Whitespace;

/**
 * This class represents the type of an external Java object returned by an
 * extension function, or supplied as an external variable/parameter.
 */
public class ExternalObjectType implements AtomicType {
    private Class javaClass;
    private Configuration config;
    int fingerprint;
    int baseFingerprint = -1;

    /**
     * Create an external object type
     * 
     * @param javaClass
     *            the Java class to which this type corresponds
     * @param config
     *            the Saxon configuration
     */

    public ExternalObjectType(Class javaClass, Configuration config) {
        this.javaClass = javaClass;
        this.config = config;
        final String localName = javaClass.getName().replace('$', '_');
        fingerprint = config.getNamePool().allocate("",
                NamespaceConstant.JAVA_TYPE, localName);
    }

    /**
     * Get the local name of this type
     * 
     * @return the local name of this type definition, if it has one. Return
     *         null in the case of an anonymous type.
     */

    public String getName() {
        return config.getNamePool().getLocalName(fingerprint);
    }

    /**
     * Get the target namespace of this type
     * 
     * @return the target namespace of this type definition, if it has one.
     *         Return null in the case of an anonymous type, and in the case of
     *         a global type defined in a no-namespace schema.
     */

    public String getTargetNamespace() {
        return config.getNamePool().getURI(fingerprint);
    }

    /**
     * Return true if this is an external object type, that is, a Saxon-defined
     * type for external Java or .NET objects
     */

    public boolean isExternalType() {
        return true;
    }

    /**
     * Determine whether this is a built-in type or a user-defined type
     * 
     * @return false - external types are not built in
     */

    public boolean isBuiltInType() {
        return false;
    }

    /**
     * Determine whether the type is abstract, that is, whether it cannot have
     * instances that are not also instances of some concrete subtype
     * 
     * @return false - external types are not abstract
     */

    public boolean isAbstract() {
        return false;
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
        return false;
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
     * Determine whether the atomic type is ordered, that is, whether less-than
     * and greater-than comparisons are permitted
     * 
     * @return true if ordering operations are permitted
     */

    public boolean isOrdered() {
        return false;
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
     * Get the validation status - always valid
     */
    public final int getValidationStatus() {
        return VALIDATED;
    }

    /**
     * Returns the value of the 'block' attribute for this type, as a
     * bit-signnificant integer with fields such as
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
     * Get the namecode of the name of this type. This includes the prefix from
     * the original type declaration: in the case of built-in types, there may
     * be a conventional prefix or there may be no prefix.
     */

    public int getNameCode() {
        return fingerprint;
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
        return BuiltInAtomicType.ANY_ATOMIC;
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
        return this;
    }

    /**
     * Get the primitive type corresponding to this item type. For item(), this
     * is Type.ITEM. For node(), it is Type.NODE. For specific node kinds, it is
     * the value representing the node kind, for example Type.ELEMENT. For
     * anyAtomicValue it is Type.ATOMIC. For numeric it is Type.NUMBER. For
     * other atomic types it is the primitive type as defined in XML Schema,
     * except that INTEGER is considered to be a primitive type.
     */

    public int getPrimitiveType() {
        return StandardNames.XS_ANY_ATOMIC_TYPE;
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
        return (other.getFingerprint() == getFingerprint());
    }

    /**
     * Get the relationship of this external object type to another external
     * object type
     * 
     * @param other
     *            the other external object type
     * @return the relationship of this external object type to another external
     *         object type, as one of the constants in class
     *         {@link TypeHierarchy}, for example {@link TypeHierarchy#SUBSUMES}
     */

    public int getRelationship(ExternalObjectType other) {
        Class j2 = other.javaClass;
        if (javaClass.equals(j2)) {
            return TypeHierarchy.SAME_TYPE;
        } else if (javaClass.isAssignableFrom(j2)) {
            return TypeHierarchy.SUBSUMES;
        } else if (j2.isAssignableFrom(javaClass)) {
            return TypeHierarchy.SUBSUMED_BY;
        } else if (javaClass.isInterface() || j2.isInterface()) {
            return TypeHierarchy.OVERLAPS; // there may be an overlap, we play
                                           // safe
        } else {
            return TypeHierarchy.DISJOINT;
        }
    }

    public String getDescription() {
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
     * @throws SchemaException
     *             if the derivation is not allowed
     */

    public void checkTypeDerivationIsOK(SchemaType type, int block)
            throws XMLException {
        // return;
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
     * @return true, this is considered to be an atomic type
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
     * @return one of PRESERVE, REPLACE, COLLAPSE
     * @param th
     *            the type hierarchy cache
     */

    public int getWhitespaceAction(TypeHierarchy th) {
        return Whitespace.PRESERVE;
    }

    /**
     * Apply the whitespace normalization rules for this simple type
     * 
     * @param value
     *            the string before whitespace normalization
     * @return the string after whitespace normalization
     */

    public CharSequence applyWhitespaceNormalization(CharSequence value)
            throws XMLException {
        return value;
    }

    /**
     * Returns the built-in base type this type is derived from.
     * 
     * @return the first built-in type found when searching up the type
     *         hierarchy
     */
    public SchemaType getBuiltInBaseType() {
        return this;
    }

    /**
     * Test whether this simple type is namespace-sensitive, that is, whether it
     * is derived from xs:QName or xs:NOTATION
     * 
     * @return true if this type is derived from xs:QName or xs:NOTATION
     */

    public boolean isNamespaceSensitive() {
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
     * Get the Java class to which this external object type corresponds
     * 
     * @return the corresponding Java class
     */

    public Class getJavaClass() {
        return javaClass;
    }

    public ItemType getSuperType(TypeHierarchy th) {
        if (javaClass == Object.class) {
            return BuiltInAtomicType.ANY_ATOMIC;
        }
        Class javaSuper = javaClass.getSuperclass();
        if (javaSuper == null) {
            // this happens for an interface
            return BuiltInAtomicType.ANY_ATOMIC;
        }
        return new ExternalObjectType(javaSuper, config);
    }

    public int getFingerprint() {
        return fingerprint;
    }

    public String toString() {
        String name = javaClass.getName();
        name = name.replace('$', '-');
        return "java:" + name;
    }

    public String getDisplayName() {
        return toString();
    }
}
