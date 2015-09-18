package com.cyngn.exovert.generate.server.rest.types;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Group type for generating {@link DataTypes} in
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
     * List of {@link DataTypes}
     */
    @JsonProperty
    public DataTypes dataTypes;
}
