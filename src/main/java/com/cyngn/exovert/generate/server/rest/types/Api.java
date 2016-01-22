package com.cyngn.exovert.generate.server.rest.types;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Type specification for API
 *
 * @author asarda@cyngn.com (Ajay Sarda) 9/8/15.
 */
public class Api {

    /**
     * API Name
     */
    @JsonProperty
    public String name;

    /**
     * API description
     */
    @JsonProperty
    public String description;

    /**
     * Routing path for the api
     */
    @JsonProperty
    public String path;

    /**
     * {@link io.vertx.core.http.HttpMethod} associated with the API
     *
     * Currently supported http_methods are GET, POST, DELETE
     */
    @JsonProperty("http_method")
    public String httpMethod;

    /**
     * Request object
     */
    @JsonProperty
    public Request request;

    /**
     * Response object
     */
    @JsonProperty
    public Response response;
}