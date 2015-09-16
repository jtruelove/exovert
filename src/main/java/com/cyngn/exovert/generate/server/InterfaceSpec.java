package com.cyngn.exovert.generate.server;

import com.cyngn.exovert.generate.server.types.Api;
import com.cyngn.exovert.generate.server.types.Type;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Server Interface for Specification types for APIs,
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
     * List of {@link Type}
     */
    @JsonProperty
    @JsonIgnore
    public List<Type> types;
}
