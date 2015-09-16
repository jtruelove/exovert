package com.cyngn.exovert.generate.server.types;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author asarda@cyngn.com (Ajay Sarda) 9/8/15.
 */
public class Validation {
    @JsonProperty
    public LengthValidation length;
}
