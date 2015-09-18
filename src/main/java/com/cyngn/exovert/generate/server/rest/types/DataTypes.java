package com.cyngn.exovert.generate.server.rest.types;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Contains all the data types defined in api definition file
 *
 * @author asarda@cyngn.com (Ajay Sarda) 9/14/15.
 */
public class DataTypes {

    /**
     * List of enumerated types
     */
    @JsonProperty
    public List<EnumType> enumTypes;

    /**
     * List of class types
     */
    @JsonProperty
    public List<ClassType> classTypes;
}
