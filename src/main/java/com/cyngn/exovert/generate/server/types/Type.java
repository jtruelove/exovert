package com.cyngn.exovert.generate.server.types;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author asarda@cyngn.com (Ajay Sarda) 9/8/15.
 */
public class Type {
    @JsonProperty("fields")
    List<Field> fields;
}
