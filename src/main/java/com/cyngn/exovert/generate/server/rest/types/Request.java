package com.cyngn.exovert.generate.server.rest.types;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Type specification for API request object
 *
 * @author asarda@cyngn.com (Ajay Sarda) 9/8/15.
 */
public class Request {
    /**
     * List of fields
     */
    @JsonProperty("fields")
    public List<Field> fields;
}
