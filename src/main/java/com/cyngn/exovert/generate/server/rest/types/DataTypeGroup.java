package com.cyngn.exovert.generate.server.rest.types;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Group type for generating {@link EnumType} and {@link ClassType} in
 * package namespace aka grouping of types at package namespace.
 *
 * @author asarda@cyngn.com (Ajay Sarda) 9/17/15.
 */
public class DataTypeGroup {
    /**
     * package namespace
     */
    @JsonProperty
    public String namespace;

    /**
     * List of enumerated types
     */
    @JsonProperty("enum_types")
    public List<EnumType> enumTypes;

    /**
     * List of class types
     */
    @JsonProperty("class_types")
    public List<ClassType> classTypes;
}
