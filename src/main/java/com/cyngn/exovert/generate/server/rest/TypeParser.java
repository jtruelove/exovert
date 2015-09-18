package com.cyngn.exovert.generate.server.rest;

import com.google.common.base.Preconditions;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Type parser to parse
 *
 * Primitives, references, list, map from type string
 *
 * Employs {@link TypeMap} to look up type.
 *
 * @author asarda@cyngn.com (Ajay Sarda) 9/17/15.
 */
public class TypeParser {

    private static final String MAP_PATTERN = "Map<\\s*(\\S+)\\s*,\\s*(\\S+)\\s*>";
    private static final String LIST_PATTERN = "List<\\s*(\\S+)\\s*>";

    /**
     * Parses the type string and return the TypeName for it
     *
     * @param type - type string
     * @param typeMap - type mapping
     * @return {@link TypeName}
     */
    public static TypeName parse(String type, TypeMap typeMap) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(type), "type cannot be empty or null");
        Preconditions.checkArgument(typeMap != null, "typeMap == null");

        if (isList(type)) {
            return parseList(type, typeMap);
        } else if (isMap(type)) {
            return parseMap(type, typeMap);
        } else {
            return typeMap.getTypeName(type);
        }
    }

    private static TypeName parseList(String type, TypeMap typeMap) {
        ClassName list = (ClassName) typeMap.getTypeName("List");
        return ParameterizedTypeName.get(list,
                typeMap.getTypeName(getListType(type)));
    }

    private static TypeName parseMap(String type, TypeMap typeMap) {
        ClassName map = (ClassName) typeMap.getTypeName("Map");
        return ParameterizedTypeName.get(map,
                typeMap.getTypeName(getMapKeyType(type)),
                typeMap.getTypeName(getMapValueType(type)));
    }

    /**
     * Checks if the given type string is list type
     * @param type - type string
     * @return true if type is list type
     */
    public static boolean isList(String type) {
        return type.startsWith("List");
    }

    /**
     * Checks if the given type string is map type
     * @param type - type string
     * @return true if type is map type
     */
    public static boolean isMap(String type) {
        return type.startsWith("Map");
    }

    /**
     * Gets the element type in list type
     * @param type - type string
     * @return T if the type is List<T>
     */
    public static String getListType(String type) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(type), "type cannot be empty or null");

        if (isList(type)){
            Pattern pattern = Pattern.compile(LIST_PATTERN);
            Matcher m = pattern.matcher(type);
            if (m.find()) {
                return m.group(1);
            } else {
                throw new IllegalArgumentException("Invalid element type in list");
            }
        }
        throw new IllegalArgumentException("Type: " + type + " is not a list");
    }

    /**
     * Gets the key type in map type
     * @param type - type string
     * @return K if the type is Map<K,V>
     */
    public static String getMapKeyType(String type) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(type), "type cannot be empty or null");

        if (isMap(type)){
            Pattern pattern = Pattern.compile(MAP_PATTERN);
            Matcher m = pattern.matcher(type);
            if (m.find()) {
                return m.group(1);
            } else {
                throw new IllegalArgumentException("Invalid types in map: " + type);
            }
        }
        throw new IllegalArgumentException("Type: " + type + " is not a map");
    }

    /**
     * Gets the value type in map type
     * @param type - type String
     * @return V if the type is Map<K,V>
     */
    public static String getMapValueType(String type) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(type), "type cannot be empty or null");

        if (isMap(type)){
            Pattern pattern = Pattern.compile(MAP_PATTERN);
            Matcher m = pattern.matcher(type);
            if (m.find()) {
                return m.group(2);
            } else {
                throw new IllegalArgumentException("Invalid types in map: " + type);
            }
        }
        throw new IllegalArgumentException("Type: " + type + " is not a list");
    }
}
