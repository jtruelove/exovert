package com.cyngn.exovert.generate.server.types;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Range validation on length of the objects.
 *
 * This can be used to validate the length of the string,
 * domain of the integer,  batch size etc for request fields.
 *
 * @author asarda@cyngn.com (Ajay Sarda) 9/8/15.
 */
public class LengthValidation {

    /**
     * Minimum length.
     *
     * For Strings, this can be set to non-zero to define mandate for
     * non-empty strings
     */
    @JsonProperty
    public int min;

    /**
     * Maximum length.
     */
    @JsonProperty
    public int max;
}
