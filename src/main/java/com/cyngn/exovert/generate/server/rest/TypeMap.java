package com.cyngn.exovert.generate.server.rest;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

/**
 * Defines the mapping of types defined in api configuration json to
 * {@link TypeName}
 *
 * @author asarda@cyngn.com (Ajay Sarda) on 9/8/15.
 */
public interface TypeMap {

    /**
     * Gets the {@link TypeMap} object for the {@code type}
     *
     * {@code type} could be primitive types (Integer, Boolean), reference types (String)
     * or user defined types (MyType)
     *
     * TODO: support collections
     *
     * @param type - type string
     * @return {@link TypeName}
     */
    TypeName getTypeName(String type);

    /**
     * Registers the type and its type name with TypeMap.
     *
     * This can be used by callers to register any custom types and classes and use it
     *
     * @param type - type string
     * @param typeName - type name
     */
    void registerType(String type, TypeName typeName);

    /**
     * Registers the type and its type name with TypeMap.
     * <p>
     * This can be used by callers to register any custom types and classes and use it
     *
     * @param type     - type string
     * @param typeName - type name
     * @param isEnum   - whether it is enum or not
     */
    void registerType(String type, TypeName typeName, boolean isEnum);

    /**
     * Checks if the given type is enumerated or not
     *
     * @param type - type string
     * @return true if type is enumerated type, otherwise false
     */
    boolean isEnumeratedType(String type);

    /**
     * Get the type conversion code block to convert {@link String}
     * to type
     * @param type - type String
     * @param cb - code block
     * @return - returns CodeBlock decorated with conversion code.
     */
    CodeBlock getTypeConverter(String type, CodeBlock cb);

    /**
     * Returns the default implementation of {@link TypeMap}
     *
     * @return {@link TypeName}
     */
    static TypeMap create() {
        return new TypeMapImpl();
    }
}
