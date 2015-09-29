package com.cyngn.exovert.generate.server.rest;

import com.cyngn.exovert.generate.server.rest.types.Api;
import com.cyngn.exovert.generate.server.rest.types.DataTypes;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Client/Server Interface for Specification types for APIs,
 *
 * request, response, request data types, response data types
 *
 * @author asarda@cyngn.com (Ajay Sarda) 9/8/15.
 */
public class InterfaceSpec {
    /**
     * package namespace
     */
    @JsonProperty
    public String namespace;

    /**
     * List of {@link Api}
     */
    @JsonProperty
    public List<Api> apis;

    /**
     * List of {@link DataTypes}
     */
    @JsonProperty("data_types")
    public DataTypes dataTypes;
}
