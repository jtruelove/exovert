package com.cyngn.exovert.generate.server.rest;

import com.cyngn.exovert.generate.server.rest.types.Api;
import com.cyngn.exovert.generate.server.rest.types.Field;
import com.cyngn.exovert.generate.server.rest.utils.Constants;
import com.cyngn.exovert.generate.server.rest.utils.RestGeneratorHelper;
import com.cyngn.vertx.web.HttpHelper;
import com.cyngn.vertx.web.JsonUtil;
import com.cyngn.vertx.web.RestApi;
import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang.StringUtils;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Helper methods to Generates method spec for {@link RestServerGenerator}
 *
 * @author asarda@cyngn.com (Ajay Sarda) 9/11/15.
 */
public class MethodGenerator {
    private GenerationContext context;

    public MethodGenerator(GenerationContext context) {
        this.context = context;
    }

    /**
     * Adds post handle method to Api class.
     *
     * <p>
     * Generated code looks like
     * </p>
     * <pre>
     * public final void handlePost(final RoutingContext context) {
     *     HttpServerRequest request = context.request();
     *     try {
     *         if (request.isEnded()) {
     *             CreateRequest createRequest = JsonUtil.parseJsonToObject(context.getBody().toString(), CreateRequest.class);
     *             validate(context, createRequest);
     *         } else {
     *             request.bodyHandler(body -> {
     *                 CreateRequest createRequest = JsonUtil.parseJsonToObject(body.toString(), CreateRequest.class);
     *                 validate(context, createRequest);
     *             });
     *         }
     *     } catch (Exception ex)  {
     *         logger.debug("Bad request, ex:" + ex);
     *         HttpHelper.processResponse(request.response(), HttpResponseStatus.BAD_REQUEST.code());
     *     }
     * }
     * </pre>
     * @param api       - api Object
     * @param namespace - package namespace
     * @return -{@link MethodSpec}
     */
    MethodSpec getHandlePostMethodSpec(Api api, String namespace) {
        Preconditions.checkArgument(api != null, "api == null");
        Preconditions.checkArgument(StringUtils.isNotEmpty(namespace), "package namespace cannot be empty or null");

        return MethodSpec.methodBuilder(RestGeneratorHelper.getHandlerName(api.httpMethod))
            .addModifiers(Modifier.FINAL)
            .addJavadoc("Handles POST request\n")
            .addParameter(RoutingContext.class, "context", Modifier.FINAL)
            .addStatement("$T request = context.request()", HttpServerRequest.class)
            .addModifiers(Modifier.PUBLIC)
            .beginControlFlow("try")
            .beginControlFlow("if (request.isEnded())")
            .addStatement("$L $L = JsonUtil.parseJsonToObject(context.getBody().toString(), $T.class)",
                    RestGeneratorHelper.getRequestObjectName(api.name),
                    RestGeneratorHelper.getRequestVariableName(api.name),
                    ClassName.get(RestGeneratorHelper.getTypesNamespace(namespace), RestGeneratorHelper.getRequestObjectName(api.name)))
            .addStatement("validate(context, $L)", RestGeneratorHelper.getRequestVariableName(api.name))
            .nextControlFlow("else")
            .addCode("request.bodyHandler(body -> " +
                    "{\n")
            .addStatement("$L $L = $T.parseJsonToObject(body.toString(), $T.class)",
                    RestGeneratorHelper.getRequestObjectName(api.name),
                    RestGeneratorHelper.getRequestVariableName(api.name),
                    JsonUtil.class,
                    ClassName.get(RestGeneratorHelper.getTypesNamespace(namespace), RestGeneratorHelper.getRequestObjectName(api.name)))
            .addStatement("validate(context, $L)", RestGeneratorHelper.getRequestVariableName(api.name))
            .addCode("});\n")
            .endControlFlow()
            .nextControlFlow("catch (Exception ex) ")
            .addStatement("logger.debug(\"Bad request, ex:\" + ex)")
            .addStatement("$T.processResponse(request.response(), $T.BAD_REQUEST.code())", HttpHelper.class, HttpResponseStatus.class)
            .endControlFlow()
            .build();
    }

    /**
     * Adds get handle method to Api class.
     *
     * <p>
     * Generated code looks like
     * </p>
     * <pre>
     * public final void handleGet(final RoutingContext context) {
     *     HttpServerRequest request = context.request();
     *     try {
     *         GetRequest getRequest = new GetRequest();
     *         if (request.getParam("name") != null) {
     *             getRequest.setName(request.getParam("name"));
     *         }
     *         validate(context, getRequest);
     *     } catch (Exception ex)  {
     *         logger.debug("Bad request, ex:" + ex);
     *         HttpHelper.processResponse(request.response(), HttpResponseStatus.BAD_REQUEST.code());
     *     }
     * }
     * </pre>
     *
     * @param api        - api Object
     * @param namespace  - package namespace
     * @return - {@link MethodSpec}
     */
    MethodSpec getHandleGetMethodSpec(Api api, String namespace) {
        Preconditions.checkArgument(api != null, "api == null");
        Preconditions.checkArgument(StringUtils.isNotEmpty(namespace), "package namespace cannot be empty or null");

        return MethodSpec.methodBuilder(RestGeneratorHelper.getHandlerName(api.httpMethod))
            .addModifiers(Modifier.FINAL)
            .addJavadoc("Handles GET request\n")
            .addParameter(RoutingContext.class, "context", Modifier.FINAL)
            .addStatement("$T request = context.request()", HttpServerRequest.class)
            .addModifiers(Modifier.PUBLIC)
            .beginControlFlow("try")
            .addCode(getRequestCodeBlock(api, namespace))
            .addStatement("validate(context, $L)", RestGeneratorHelper.getRequestVariableName(api.name))
            .nextControlFlow("catch (Exception ex) ")
            .addStatement("logger.debug(\"Bad request, ex:\" + ex)")
            .addStatement("$T.processResponse(request.response(), $T.BAD_REQUEST.code())", HttpHelper.class, HttpResponseStatus.class)
            .endControlFlow()
            .build();
    }

    /**
     * Adds delete handle method to Api class.
     *
     *  <p>
     * Generated code looks like
     * </p>
     *
     * <pre>
     * public final void handleDelete(final RoutingContext context) {
     *     HttpServerRequest request = context.request();
     *     try {
     *         DeleteRequest deleteRequest = new DeleteRequest();
     *         if (request.getParam("name") != null) {
     *             deleteRequest.setName(request.getParam("name"));
     *         }
     *         validate(context, deleteRequest);
     *     } catch (Exception ex)  {
     *         logger.debug("Bad request, ex:" + ex);
     *         HttpHelper.processResponse(request.response(), HttpResponseStatus.BAD_REQUEST.code());
     *     }
     * }
     * </pre>
     * @param api        - api Object
     * @param namespace  - package namespace
     * @return - {@link MethodSpec}
     */
    MethodSpec getHandleDeleteMethodSpec(Api api, String namespace) {
        Preconditions.checkArgument(api != null, "api == null");
        Preconditions.checkArgument(StringUtils.isNotEmpty(namespace), "package namespace cannot be empty or null");

        return MethodSpec.methodBuilder(RestGeneratorHelper.getHandlerName(api.httpMethod))
            .addModifiers(Modifier.FINAL)
            .addJavadoc("Handles DELETE request\n")
            .addParameter(RoutingContext.class, "context", Modifier.FINAL)
            .addStatement("$T request = context.request()", HttpServerRequest.class)
            .addModifiers(Modifier.PUBLIC)
            .beginControlFlow("try")
            .addCode(getRequestCodeBlock(api, namespace))
            .addStatement("validate(context, $L)", RestGeneratorHelper.getRequestVariableName(api.name))
            .nextControlFlow("catch (Exception ex) ")
            .addStatement("logger.debug(\"Bad request, ex:\" + ex)")
            .addStatement("$T.processResponse(request.response(), $T.BAD_REQUEST.code())", HttpHelper.class, HttpResponseStatus.class)
            .endControlFlow()
            .build();
    }

    /**
     * Gets request code block for get, delete request.
     *
     * <p>
     * Generated code looks like
     * </p>
     * <pre>
     *  DeleteRequest deleteRequest = new DeleteRequest();
     *  if (request.getParam("name") != null) {
     *      deleteRequest.setName(request.getParam("name"));
     *  }
     * </pre>
     * @param api       - api Object
     * @param namespace - package namespace
     * @return - {@link CodeBlock}
     */
    private CodeBlock getRequestCodeBlock(Api api, String namespace) {
        Preconditions.checkArgument(api != null, "api == null");
        Preconditions.checkArgument(StringUtils.isNotEmpty(namespace), "package namespace cannot be empty or null");

        CodeBlock.Builder builder = CodeBlock.builder();
        builder.addStatement("$L $L = new $T()",
                RestGeneratorHelper.getBuilderTypeName(RestGeneratorHelper.getRequestObjectName(api.name)),
                "builder",
                ClassName.get(RestGeneratorHelper.getTypesNamespace(namespace), RestGeneratorHelper.getBuilderTypeName(RestGeneratorHelper.getRequestObjectName(api.name))));

        for (Field field : api.request.fields) {
            // TODO: generate objects for custom defined types.
            builder.add("if (");
            builder.add("request.getParam($S)", CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.name));
            builder.add(" != null) {\n").indent();
            builder.add("$L.$L(",
                    "builder",
                    RestGeneratorHelper.getFieldName(field.name));
            builder.add(context.typeMap.getTypeConverter(RestGeneratorHelper.getTypeNameString(field.type),
                    CodeBlock.builder().add("request.getParam($S)", CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.name)).add(")").build()));
            builder.add(";\n");
            builder.unindent().add("}\n");
        }

        builder.addStatement("$L $L = builder.build()", RestGeneratorHelper.getRequestObjectName(api.name), RestGeneratorHelper.getRequestVariableName(api.name));
        return builder.build();
    }

    /**
     * Generates validate request method spec for API.
     *
     * <p>
     * Generated code looks like
     * </p>
     *
     * <pre>
     *  private final void validate(final RoutingContext, final DeleteRequest deleteRequest) {
     *      final ValidationResult validationResult = deleteRequest.validate();
     *      if (ValidationResult.SUCCESS.equals(validationResult)) {
     *          process(context, deleteRequest);
     *      } else {
     *          HttpHelper.processResponse(context.request().response(), HttpResponseStatus.BAD_REQUEST.code());
     *      }
     * }
     * </pre>
     * @param api       - api Object
     * @param namespace - package namespace
     * @return - {@link MethodSpec}
     */
    MethodSpec getValidateMethodSpec(Api api, String namespace) {
        Preconditions.checkArgument(api != null, "api == null");
        Preconditions.checkArgument(StringUtils.isNotEmpty(namespace), "package namespace cannot be empty or null");

        return MethodSpec.methodBuilder("validate")
                .addJavadoc("Handles validation of request\n")
                .addParameter(RoutingContext.class, "context", Modifier.FINAL)
                .addParameter(ClassName.get(RestGeneratorHelper.getTypesNamespace(namespace), RestGeneratorHelper.getRequestObjectName(api.name)),
                        RestGeneratorHelper.getRequestVariableName(api.name), Modifier.FINAL)
                .addModifiers(Modifier.PRIVATE)
                .addModifiers(Modifier.FINAL)
                .beginControlFlow("if ($L == null)", RestGeneratorHelper.getRequestVariableName(api.name))
                .addStatement("$T.processResponse(context.request().response(), $T.BAD_REQUEST.code())", HttpHelper.class, HttpResponseStatus.class)
                .addStatement("return")
                .endControlFlow()
                .addStatement("final $T validationResult = $L.validate()", ValidationResult.class,
                        CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, RestGeneratorHelper.getRequestObjectName(api.name)))
                .beginControlFlow("if (ValidationResult.SUCCESS.equals(validationResult))")
                .addStatement("process(context, $L)", RestGeneratorHelper.getRequestVariableName(api.name))
                .nextControlFlow("else")
                .addStatement("$T.processResponse(validationResult.errorMsg, context.request().response(), $T.BAD_REQUEST.code())", HttpHelper.class, HttpResponseStatus.class)
                .endControlFlow()
                .build();
    }

    /**
     * Generates abstract process method spec for API
     *
     * <p>
     * Generated code looks like
     * </p>
     * <pre>
     *     public abstract void process(final RoutingContext context, final CreateRequest request);
     * </pre>
     * @param api       - api Object
     * @param namespace - package namespace
     * @return - {@link MethodSpec}
     */
    MethodSpec getProcessMethodSpec(Api api, String namespace) {
        Preconditions.checkArgument(api != null, "api == null");
        Preconditions.checkArgument(StringUtils.isNotEmpty(namespace), "package namespace cannot be empty or null");

        return MethodSpec.methodBuilder("process")
                .addJavadoc("Processes the request\n")
                .addParameter(RoutingContext.class, "context", Modifier.FINAL)
                .addParameter(ClassName.get(RestGeneratorHelper.getTypesNamespace(namespace), RestGeneratorHelper.getRequestObjectName(api.name)),
                        RestGeneratorHelper.getRequestVariableName(api.name), Modifier.FINAL)
                .addModifiers(Modifier.ABSTRACT)
                .addModifiers(Modifier.PUBLIC).build();
    }

    /**
     * Generates the getSupportedAPI method for API.
     * <p>
     * Generated code looks like
     * </p>
     * <pre>
     * @Override
     * public RestApi.RestApiDescriptor[] supportedApi() { return supportedApi; }
     * </pre>
     * @return - {@link MethodSpec}
     */
    MethodSpec getSupportedApi() {
        return MethodSpec.methodBuilder("supportedApi")
                .addAnnotation(Override.class)
                .addStatement("$L", "return " + Constants.SUPPORTED_API_FIELD)
                .returns(TypeName.get(RestApi.RestApiDescriptor[].class))
                .addModifiers(Modifier.PUBLIC)
                .build();
    }

    MethodSpec getGetHttpMethod(String httpMethod) {
        MethodSpec.Builder methodBuilder =
                MethodSpec.methodBuilder("getHttpMethod")
                .addJavadoc("Returns the http method\n")
                .addModifiers(Modifier.FINAL, Modifier.PROTECTED)
                .returns(HttpMethod.class);


        if (httpMethod.equalsIgnoreCase(Constants.HTTP_METHOD_POST)) {
            methodBuilder.addStatement("return $T.POST", HttpMethod.class);
        } else if (httpMethod.equalsIgnoreCase(Constants.HTTP_METHOD_GET)) {
            methodBuilder.addStatement("return $T.GET", HttpMethod.class);
        } else if (httpMethod.equalsIgnoreCase(Constants.HTTP_METHOD_DELETE)) {
            methodBuilder.addStatement("return $T.DELETE", HttpMethod.class);
        }

        return methodBuilder.build();
    }

    /**
     * Generates {@link MethodSpec} for validate method for Request types
     *
     * <p>
     * Generated code looks like
     * </p>
     * <pre>
     * @Override
     * public ValidationResult validate() {
     *     if (name == null) { new ValidationResult(false, "name cannot be null"); }
     *     if (StringUtils.isEmpty(name)) { return new ValidationResult(false, "name cannot be empty"); }
     *     if (name.length() > 1000) { return new ValidationResult(false, "length of name exceeds the max allowed length of 1000"); }
     *     if (quantity == null) { return new ValidationResult(false, "Quantity cannot be null"); }
     *     if (price == null) { return new ValidationResult(false, "Price cannot be null"); }
     *     return ValidationResult.SUCCESS;
     * }
     * </pre>
     * @param fields - fields of the type
     * @return - {@link MethodSpec}
     */
    MethodSpec getValidateMethodSpec(List<Field> fields) {
        MethodSpec.Builder validationMethodBuilder =
                MethodSpec.methodBuilder(Constants.VALIDATE)
                        .addJavadoc("Validates request object\n")
                        .addJavadoc("\n@return {@link ValidationResult}\n")
                        .returns(ValidationResult.class)
                        .addModifiers(Modifier.PUBLIC);

        for (Field field : fields) {
            if (field.required) {
                // glue in validation for required fields
                validationMethodBuilder.addCode("if ($N == null) ", RestGeneratorHelper.getFieldName(field.name));
                validationMethodBuilder.addCode("{ return new ValidationResult(false, \"$N cannot be null\"); }\n",
                        RestGeneratorHelper.getFieldName(field.name));
            }

            if (field.validation != null) {
                //go through each type of validation

                if (field.validation.length != null) {
                    if (field.type.equals("String")) {
                        if (field.validation.length.min > 0) {
                            // has length restriction
                            validationMethodBuilder.addCode("if ($T.isEmpty($N)) ", StringUtils.class, RestGeneratorHelper.getFieldName(field.name));
                            validationMethodBuilder.addCode("{ return new ValidationResult(false, \"$N cannot be empty\"); }\n",
                                    RestGeneratorHelper.getFieldName(field.name));
                        }

                        if (field.validation.length.max > 0) {
                            validationMethodBuilder.addCode("if ($N.length() > $L) ", RestGeneratorHelper.getFieldName(field.name), field.validation.length.max);
                            validationMethodBuilder.addCode("{ return new ValidationResult(false, \"length of $N exceeds the max allowed length of $L\"); }\n",
                                    RestGeneratorHelper.getFieldName(field.name), field.validation.length.max);
                        }
                    }
                }
            }
        }

        validationMethodBuilder.addStatement("return ValidationResult.SUCCESS");
        return validationMethodBuilder.build();
    }

    /**
     * Generates {@link MethodSpec} for set method
     *
     * <p>
     * Generated code looks like
     * </p>
     *
     * <pre>
     *  public void setName(String name) {
     *      this.name = name;
     *  }
     * </pre>
     *
     * @param name - field name
     * @param typeName - field type name
     * @return - {@link MethodSpec}
     */
    MethodSpec getSetMethodSpec(String name, TypeName typeName) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(name), "field name cannot be empty or null");
        Preconditions.checkArgument(typeName != null, "typeName == null");

        return MethodSpec.methodBuilder(RestGeneratorHelper.getSetMethodName(name))
                .addModifiers(Modifier.PUBLIC)
                .addParameter(typeName, name)
                .addStatement("this.$N = $N", name, name)
                .build();
    }

    /**
     * Generates {@link MethodSpec} for get method
     *
     * <p>
     * Generated code looks like
     * </p>
     *
     * <pre>
     * public String getName() {
     *     return name;
     * }
     * </pre>
     *
     * @param name - field name
     * @param typeName - field type
     * @return - {@link MethodSpec}
     */
    MethodSpec getGetMethodSpec(String name, TypeName typeName) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(name), "field name cannot be empty or null");
        Preconditions.checkArgument(typeName != null, "typeName == null");

        return MethodSpec.methodBuilder(RestGeneratorHelper.getGetMethodName(name))
                .addModifiers(Modifier.PUBLIC)
                .returns(typeName)
                .addStatement("return $N", name)
                .build();
    }

    /**
     * Generates methods for hashCode method.
     *
     * @param fieldNames - list of fields in the type
     * @return - {@link MethodSpec}
     */
    MethodSpec getHashCodeSpec(List<String> fieldNames) {
        MethodSpec.Builder hashCodeBuilder =  MethodSpec.methodBuilder("hashCode")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.INT);

        hashCodeBuilder.addStatement("return $T.hash($L)", Objects.class, Joiner.on(", ").join(fieldNames));
        return hashCodeBuilder.build();
    }

    /**
     * Returns method spec for Object.equals() method.
     * @param className - class name for which equals() is needed
     * @param fieldNames - list of fields of the class.
     */
    MethodSpec getEqualsCodeSpec(String className, List<String> fieldNames) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(className), "class name cannot be empty or null");

        MethodSpec.Builder equalsBuilder =  MethodSpec.methodBuilder("equals")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Object.class, "obj")
                .returns(TypeName.BOOLEAN);

        List<String> objectsEqualsList = new ArrayList<>();

        for (String fieldName : fieldNames) {
            objectsEqualsList.add("Objects.equals(" + fieldName + ", other." + fieldName + ")");
        }

        String objectEqualsString = Joiner.on(" &&\n").join(objectsEqualsList);

        return equalsBuilder.beginControlFlow( "if (obj == this) ")
                .addStatement("return true")
                .endControlFlow()
                .addCode("\n")
                .beginControlFlow("if (obj instanceof $L)", className)
                .addStatement("$L other = ($L) obj", className, className)
                .addStatement("return $L", objectEqualsString)
                .endControlFlow()
                .addCode("\n")
                .addStatement("return false")
                .build();
    }

    /**
     * Returns MethodSpec for Object.toString() method
     */
    MethodSpec getToStringCodeSpec(String className, List<String> fieldNames) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("toString")
                .addAnnotation(Override.class)
                .returns(String.class)
                .addModifiers(Modifier.PUBLIC);

        builder.addCode("return $S $L\n", className + "{", "+");
        boolean first = true;
        for (String fieldName : fieldNames) {
            String fieldStr = fieldName + "=";
            if(!first) {
                fieldStr = ", " + fieldStr;
            } else {
                first = false;
            }

            builder.addCode("$S + $N +\n", fieldStr, fieldName);
        }
        builder.addStatement("$S", "}");
        return builder.build();
    }

    /**
     * Returns MethodSpec for empty constructor
     */
    MethodSpec getEmptyConstructorSpec() {
        return MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).build();
    }
}
