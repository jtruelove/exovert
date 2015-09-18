package com.cyngn.exovert.generate.server.rest.types;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

/**
 * Specification Class for Enumerated type.
 *
 * Contains list of {@link EnumValue}
 *
 * @author asarda@cyngn.com (Ajay Sarda) 9/14/15.
 */
public class EnumType {

    /**
     * Name of enum class
     */
    @JsonProperty
    public String name;

    /**
     * Set of enum values
     */
    @JsonProperty
    public Set<EnumValue> values;

    /**
     * Enum type documentation
     */
    @JsonProperty
    public String documentation;
}
