package com.cyngn.exovert.generate.server.utils;

/**
 * Contains constants for {@link com.cyngn.exovert.generate.server.InterfaceGenerator}
 *
 * @author asarda@cyngn.com (Ajay Sarda) 9/10/15.
 */
public class Constants {
    // directory constants
    public static final String GENERATED_SRC_DIRECTORY = "generated-src";
    public static final String BUILD_DIRECTORY = "build";
    public static final String INDENTATION_SPACES = "    ";

    //package name constants
    public static final String TYPES_PACKAGE_SUFFIX = "types";
    public static final String API_PACKAGE_SUFFIX = "api";

    // class name constants
    public static final String REQUEST_CLASS_SUFFIX = "Request";
    public static final String RESPONSE_CLASS_SUFFIX = "Response";
    public static final String API_NAME_PREFIX = "Abstract";
    public static final String API_NAME_SUFFIX = "Api";

    // method name constants
    public static final String VALIDATE = "validate";
    public static final String SET_METHOD_PREFIX = "set";
    public static final String GET_METHOD_PREFIX = "get";
    public static final String HANDLE_GET = "handleGet";
    public static final String HANDLE_POST = "handlePost";
    public static final String HANDLE_DELETE = "handleDelete";

    //field name constants
    public static final String SUPPORTED_API_FIELD = "supportedApi";
    public static final String API_PATH = "_API_PATH";

    // http constants
    public static final String HTTP_METHOD_GET = "GET";
    public static final String HTTP_METHOD_POST = "POST";
    public static final String HTTP_METHOD_DELETE = "DELETE";
}
