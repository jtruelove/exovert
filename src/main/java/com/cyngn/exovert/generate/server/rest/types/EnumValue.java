package com.cyngn.exovert.generate.server.rest.types;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Type for enum value
 *
 * {@link EnumType} contains the list of {@link EnumValue}
 *
 * @author asarda@cyngn.com (Ajay Sarda) 9/14/15.
 */
public class EnumValue {

    /**
     * Enum name
     */
    @JsonProperty
    public String name;

    /**
     * Enum value
     */
    @JsonProperty
    public String value;
}
