package com.cyngn.exovert.generate.server.rest;

import com.cyngn.exovert.generate.server.rest.types.DataTypeGroup;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Specifications for data types
 *
 * @author asarda@cyngn.com (Ajay Sarda) 9/17/15.
 */
public class DataTypeSpec {

    /**
     * List of {@link DataTypeGroup}
     *
     * {@link DataTypeGroup} represents the set of datatypes in package namespace.
     */
    @JsonProperty
    public List<DataTypeGroup> dataTypeGroups;
}
