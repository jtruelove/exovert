package com.cyngn.exovert.generate.server.rest.types;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Class to holds different types of validation that can be
 * applied for Type
 *
 * @author asarda@cyngn.com (Ajay Sarda) 9/8/15.
 */
public class Validation {
    /**
     * length validation
     */
    @JsonProperty
    public LengthValidation length;
}
