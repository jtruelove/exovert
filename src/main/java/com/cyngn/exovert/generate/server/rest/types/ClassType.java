package com.cyngn.exovert.generate.server.rest.types;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Specification type for Class.
 *
 * @author asarda@cyngn.com (Ajay Sarda) 9/14/15.
 */
public class ClassType {

    /**
     * Class Name
     */
    @JsonProperty
    public String name;

    /**
     * Fields of the class
     */
    @JsonProperty("fields")
    public List<Field> fields;

    /**
     * Whether class is immutable or not
     */
    @JsonProperty
    public boolean immutable;

    /**
     * Should include json annotations or not
     */
    @JsonProperty("json_annotations")
    public boolean jsonAnnotations;

    /**
     * Class Type documentation
     */
    @JsonProperty
    public String description;
}
