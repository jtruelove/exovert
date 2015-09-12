package com.cyngn.exovert.generate.server.utils;

import com.google.common.base.CaseFormat;

import java.nio.file.FileSystems;
import java.nio.file.Path;

/**
 * Rest generator Helper methods for {@link com.cyngn.exovert.generate.server.InterfaceGenerator}
 *
 * @author asarda@cyngn.com (Ajay Sarda) 9/10/15.
 */
public class RestGeneratorHelper {

    public static Path getGeneratedSourceDirectoryPath() {
        return FileSystems.getDefault().getPath(Constants.BUILD_DIRECTORY, Constants.GENERATED_SRC_DIRECTORY);
    }

    public static String getTypesNamespace(String namespace) {
        return namespace + "." + Constants.TYPES_PACKAGE_SUFFIX;
    }

    public static String getApiNamespace(String namespace) {
        return namespace + "." + Constants.API_PACKAGE_SUFFIX;
    }

    public static String getApiName(String api) {
        return Constants.API_NAME_PREFIX + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, api)
                + Constants.API_NAME_SUFFIX;
    }

    public static String getRequestObjectName(String apiName) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, apiName + Constants.REQUEST_CLASS_SUFFIX);
    }

    public static String getRequestVariableName(String apiName) {
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, RestGeneratorHelper.getRequestObjectName(apiName));
    }

    public static String getResponseObjectName(String apiName) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, apiName + Constants.RESPONSE_CLASS_SUFFIX);
    }

    public static String getSetMethodName(String fieldName) {
        return Constants.SET_METHOD_PREFIX + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, fieldName);
    }

    public static String getHandlerName(String httpMethod) {
        if (httpMethod.equalsIgnoreCase(Constants.HTTP_METHOD_GET)) {
            return Constants.HANDLE_GET;
        } else if(httpMethod.equalsIgnoreCase(Constants.HTTP_METHOD_POST)) {
            return Constants.HANDLE_POST;
        } else if(httpMethod.equalsIgnoreCase(Constants.HTTP_METHOD_DELETE)) {
            return Constants.HANDLE_GET;
        } else {
            throw new IllegalArgumentException("Invalid or unsupported httpMethod: " + httpMethod);
        }
    }
}
