package com.cyngn.exovert.generate.server.types;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Type specification for field. Field is contained within
 * {@link Request}, {@link Response} and {@link Response}
 *
 * @author asarda@cyngn.com (Ajay Sarda) 9/8/15.
 */
public class Field {
    /**
     * Field name
     */
    @JsonProperty
    public String name;

    /**
     * Type of the field.
     *
     * The type gets mapped to Java type by {@link com.cyngn.exovert.generate.server.TypeMap}
     */
    @JsonProperty
    public String type;

    /**
     * Whether required or not
     */
    @JsonProperty
    public boolean required;

    /**
     * Validation associated with the field.
     */
    @JsonProperty
    public Validation validation;
}
