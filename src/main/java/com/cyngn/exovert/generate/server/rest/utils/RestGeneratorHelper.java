package com.cyngn.exovert.generate.server.rest.utils;

import com.cyngn.exovert.generate.server.rest.DataTypeSpec;
import com.cyngn.exovert.generate.server.rest.InterfaceSpec;
import com.cyngn.exovert.generate.server.rest.RestServerGenerator;
import com.cyngn.exovert.generate.server.rest.TypeMap;
import com.cyngn.exovert.generate.server.rest.TypeParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Rest generator Helper methods for {@link RestServerGenerator}
 *
 * @author asarda@cyngn.com (Ajay Sarda) 9/10/15.
 */
public class RestGeneratorHelper {
    private static final Logger logger = LoggerFactory.getLogger(RestGeneratorHelper.class);
    private static ObjectMapper mapper = new ObjectMapper();

    private static Map<String, String> handlesMap = ImmutableMap.of(
            Constants.HTTP_METHOD_GET, Constants.HANDLE_GET, Constants.HTTP_METHOD_POST, Constants.HANDLE_POST,
            Constants.HTTP_METHOD_DELETE, Constants.HANDLE_DELETE);

    private static Set<String> supportedHttpMethods = ImmutableSet.of(
            Constants.HTTP_METHOD_GET, Constants.HTTP_METHOD_POST, Constants.HTTP_METHOD_DELETE);

    public static String getGeneratedSourceDirectory() {
        return Constants.BUILD_DIRECTORY + "/"  + Constants.GENERATED_SRC_DIRECTORY;
    }

    public static Path getDirectoryPath(String path) {
        return FileSystems.getDefault().getPath(path);
    }

    public static String getTypesNamespace(String namespace) {
        return namespace + "." + Constants.TYPES_PACKAGE_SUFFIX;
    }

    public static String getApiNamespace(String namespace) {
        return namespace + "." + Constants.API_PACKAGE_SUFFIX;
    }

    private static String getLowerCamelCaseFromSnakeCase(String str) {
        return  CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, str.toLowerCase());
    }

    private static String getUpperCamelCaseFromSnakeCase(String str) {
        return  CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, str.toLowerCase());
    }

    public static String getApiName(String api) {
        return Constants.API_NAME_PREFIX + getUpperCamelCaseFromSnakeCase(api)
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
        //fieldname is always normalized to lowerCamel
        return Constants.SET_METHOD_PREFIX + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, fieldName);
    }

    public static String getGetMethodName(String fieldName) {
        //fieldname is always normalized to lowerCamel
        return Constants.GET_METHOD_PREFIX + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, fieldName);
    }

    public static String getFieldName(String fieldName) {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, fieldName.toLowerCase());
    }

    public static String getTypeNameString(String typeName) {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, typeName.toLowerCase());
    }

    public static String getTypeName(String name, TypeMap typeMap) {
        if (TypeParser.isList(name)) {
            return String.format("List<%s>", getUpperCamelCaseFromSnakeCase(TypeParser.getListType(name)));
        } else if (TypeParser.isSet(name)) {
            return String.format("Set<%s>", getUpperCamelCaseFromSnakeCase(TypeParser.getSetType(name)));
        } else if (TypeParser.isMap(name)){
            return String.format("Map<%s,%s>", getUpperCamelCaseFromSnakeCase(TypeParser.getMapKeyType(name)),
                    getUpperCamelCaseFromSnakeCase(TypeParser.getMapValueType(name)));
        } else {
            return getUpperCamelCaseFromSnakeCase(name);
        }
    }

    public static String getBuilderTypeName(String name){
        return name + Constants.BUILDER_CLASS_SUFFIX;
    }

    public static String getBuilderVariableName(){
        return Constants.BUILDER_CLASS_NAME.toLowerCase();
    }

    public static String getHandlerName(String httpMethod) {
        String handlerName = handlesMap.get(httpMethod.toUpperCase());
        if (handlerName == null) {
            throw new IllegalArgumentException("Invalid or unsupported httpMethod: " + httpMethod);
        }
        return handlerName;
    }

    public static String getHttpMethod(String httpMethod) {
        if (supportedHttpMethods.contains(httpMethod.toUpperCase())) {
            return httpMethod.toUpperCase();
        } else {
            throw new IllegalArgumentException("Invalid or unsupported httpMethod: " + httpMethod);
        }
    }

    public static String getServiceClientName(String serviceName) {
        return serviceName + Constants.CLIENT_SUFFIX;
    }

    public static String getCallApiMethodName(String apiName) {
        return Constants.CALL_PREFIX + getUpperCamelCaseFromSnakeCase(apiName);
    }

    public static InterfaceSpec loadSpecFromFile(String filename) throws Exception {
        InterfaceSpec spec;

        try (Scanner scanner = new Scanner(new File(filename)).useDelimiter("\\A")) {
            String sconf = scanner.next();
            try {
                spec = mapper.readValue(sconf, InterfaceSpec.class);
                logger.info("Successfully loaded specification file " + filename);
            } catch (IOException e) {
                logger.error("Api definition file " + sconf + " does not contain a valid JSON object");
                throw e;
            }
        } catch (FileNotFoundException e) {
            logger.error("No api definition file found", e);
            throw e;
        }
        return spec;
    }

    public static DataTypeSpec loadTypeSpecFromFile(String filename) throws Exception {
        DataTypeSpec spec;

        try (Scanner scanner = new Scanner(new File(filename)).useDelimiter("\\A")) {
            String sconf = scanner.next();
            try {
                spec = mapper.readValue(sconf, DataTypeSpec.class);
                logger.info("Successfully loaded types file " + filename);
            } catch (IOException e) {
                logger.error("Type definition file " + sconf + " does not contain a valid JSON object");
                throw e;
            }
        } catch (FileNotFoundException e) {
            logger.error("No type definition file found", e);
            throw e;
        }
        return spec;
    }
}
