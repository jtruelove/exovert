package com.cyngn.exovert.generate.server.types;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Type Specification for Response object
 *
 * @author asarda@cyngn.com (Ajay Sarda) 9/8/15.
 */

public class Response {
    @JsonProperty("fields")
    public List<Field> fields;
}
