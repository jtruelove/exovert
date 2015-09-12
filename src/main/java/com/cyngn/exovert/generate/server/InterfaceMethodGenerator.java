package com.cyngn.exovert.generate.server;

import com.cyngn.exovert.generate.server.types.Api;
import com.cyngn.exovert.generate.server.types.Field;
import com.cyngn.exovert.generate.server.utils.Constants;
import com.cyngn.exovert.generate.server.utils.RestGeneratorHelper;
import com.cyngn.vertx.web.HttpHelper;
import com.cyngn.vertx.web.JsonUtil;
import com.cyngn.vertx.web.RestApi;
import com.google.common.base.CaseFormat;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang.StringUtils;

import javax.lang.model.element.Modifier;
import java.util.List;

/**
 * Helper methods to Generates method spec for {@link InterfaceGenerator}
 *
 * @author asarda@cyngn.com (Ajay Sarda) 9/11/15.
 */
public class InterfaceMethodGenerator {

    private static final TypeMap typeMap = TypeMap.create();

    /**
     * Adds post handle method to Api class.
     *
     * <p>
     * Generated code looks like
     * </p>
     * <pre>
     * public void handlePost(final RoutingContext context) {
     *     HttpServerRequest request = context.request();
     *     try {
     *         if (request.isEnded()) {
     *             CreateRequest createRequest = JsonUtil.parseJsonToObject(context.getBody().toString(), CreateRequest.class);
     *             validate(request, createRequest);
     *         } else {
     *             request.bodyHandler(body -> {
     *                 CreateRequest createRequest = JsonUtil.parseJsonToObject(body.toString(), CreateRequest.class);
     *                 validate(request, createRequest);
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
    static MethodSpec getHandlePostMethodSpec(Api api, String namespace) {
        return MethodSpec.methodBuilder(RestGeneratorHelper.getHandlerName(api.httpMethod))
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
            .addStatement("validate(request, $L)", RestGeneratorHelper.getRequestVariableName(api.name))
            .nextControlFlow("else")
            .addCode("request.bodyHandler(body -> " +
                    "{\n")
            .addStatement("$L $L = $T.parseJsonToObject(body.toString(), $T.class)",
                    RestGeneratorHelper.getRequestObjectName(api.name),
                    RestGeneratorHelper.getRequestVariableName(api.name),
                    JsonUtil.class,
                    ClassName.get(RestGeneratorHelper.getTypesNamespace(namespace), RestGeneratorHelper.getRequestObjectName(api.name)))
            .addStatement("validate(request, $L)", RestGeneratorHelper.getRequestVariableName(api.name))
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
     * public void handleGet(final RoutingContext context) {
     *     HttpServerRequest request = context.request();
     *     try {
     *         GetRequest getRequest = new GetRequest();
     *         if (request.getParam("name") != null) {
     *             getRequest.setName(request.getParam("name"));
     *         }
     *         validate(request, getRequest);
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
    static MethodSpec getHandleGetMethodSpec(Api api, String namespace) {
        return MethodSpec.methodBuilder(RestGeneratorHelper.getHandlerName(api.httpMethod))
            .addJavadoc("Handles GET request\n")
            .addParameter(RoutingContext.class, "context", Modifier.FINAL)
            .addStatement("$T request = context.request()", HttpServerRequest.class)
            .addModifiers(Modifier.PUBLIC)
            .beginControlFlow("try")
            .addCode(getRequestCodeBlock(api, namespace))
            .addStatement("validate(request, $L)", RestGeneratorHelper.getRequestVariableName(api.name))
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
     * public void handleDelete(final RoutingContext context) {
     *     HttpServerRequest request = context.request();
     *     try {
     *         DeleteRequest deleteRequest = new DeleteRequest();
     *         if (request.getParam("name") != null) {
     *             deleteRequest.setName(request.getParam("name"));
     *         }
     *         validate(request, deleteRequest);
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
    static MethodSpec getHandleDeleteMethodSpec(Api api, String namespace) {
        return MethodSpec.methodBuilder(RestGeneratorHelper.getHandlerName(api.httpMethod))
            .addJavadoc("Handles DELETE request\n")
            .addParameter(RoutingContext.class, "context", Modifier.FINAL)
            .addStatement("$T request = context.request()", HttpServerRequest.class)
            .addModifiers(Modifier.PUBLIC)
            .beginControlFlow("try")
            .addCode(getRequestCodeBlock(api, namespace))
            .addStatement("validate(request, $L)", RestGeneratorHelper.getRequestVariableName(api.name))
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
    private static CodeBlock getRequestCodeBlock(Api api, String namespace) {
        CodeBlock.Builder builder = CodeBlock.builder();
        builder.addStatement("$L $L = new $T()",
                RestGeneratorHelper.getRequestObjectName(api.name),
                RestGeneratorHelper.getRequestVariableName(api.name),
                ClassName.get(RestGeneratorHelper.getTypesNamespace(namespace), RestGeneratorHelper.getRequestObjectName(api.name)));

        for (Field field : api.request.fields) {
            // TODO: generate objects for custom defined types.
            builder.add("if (");
            builder.add("request.getParam($S)", CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.name));
            builder.add(" != null) {\n").indent();
            builder.add("$L.$L(",
                    RestGeneratorHelper.getRequestVariableName(api.name),
                    RestGeneratorHelper.getSetMethodName(field.name));
            builder.add(typeMap.getTypeConverter(field.type,
                    CodeBlock.builder().add("request.getParam($S)", CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.name)).build()));
            builder.unindent().add("}\n");
        }

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
     *  private final void validate(final HttpServerRequest request, final DeleteRequest deleteRequest) {
     *      final ValidationResult validationResult = deleteRequest.validate();
     *      if (ValidationResult.SUCCESS.equals(validationResult)) {
     *          process(deleteRequest);
     *      } else {
     *          HttpHelper.processResponse(request.response(), HttpResponseStatus.BAD_REQUEST.code());
     *      }
     * }
     * </pre>
     * @param api       - api Object
     * @param namespace - package namespace
     * @return - {@link MethodSpec}
     */
    static MethodSpec getValidateMethodSpec(Api api, String namespace) {
        return MethodSpec.methodBuilder("validate")
                .addJavadoc("Handles validation of request\n")
                .addParameter(HttpServerRequest.class, "request", Modifier.FINAL)
                .addParameter(ClassName.get(RestGeneratorHelper.getTypesNamespace(namespace), RestGeneratorHelper.getRequestObjectName(api.name)),
                        RestGeneratorHelper.getRequestVariableName(api.name), Modifier.FINAL)
                .addModifiers(Modifier.PRIVATE)
                .addModifiers(Modifier.FINAL)
                .addStatement("final $T validationResult = $L.validate()", ValidationResult.class,
                        CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, RestGeneratorHelper.getRequestObjectName(api.name)))
                .beginControlFlow("if (ValidationResult.SUCCESS.equals(validationResult))")
                .addStatement("process(request, $L)", RestGeneratorHelper.getRequestVariableName(api.name))
                .nextControlFlow("else")
                .addStatement("$T.processResponse(request.response(), $T.BAD_REQUEST.code())", HttpHelper.class, HttpResponseStatus.class)
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
     *     public abstract void process(final HttpServerRequest request, final CreateRequest request);
     * </pre>
     * @param api       - api Object
     * @param namespace - package namespace
     * @return - {@link MethodSpec}
     */
    static MethodSpec getProcessMethodSpec(Api api, String namespace) {
        return MethodSpec.methodBuilder("process")
                .addJavadoc("Processes the request\n")
                .addParameter(HttpServerRequest.class, "request", Modifier.FINAL)
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
    static MethodSpec getSupportedApi() {
        return MethodSpec.methodBuilder("supportedApi")
                .addAnnotation(Override.class)
                .addStatement("$L", "return " + Constants.SUPPORTED_API_FIELD)
                .returns(TypeName.get(RestApi.RestApiDescriptor[].class))
                .addModifiers(Modifier.PUBLIC)
                .build();
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
     *     if (name == null) { new ValidationResult(false, "name cannot be null");}
     *     if (StringUtils.isEmpty(name)) { new ValidationResult(false, "name cannot be empty");}
     *     if (name.length() > 1000) { new ValidationResult(false, "length of name exceeds the max allowed length of 1000");}
     *     if (Quantity == null) { new ValidationResult(false, "Quantity cannot be null");}
     *     if (Price == null) { new ValidationResult(false, "Price cannot be null");}
     *     return ValidationResult.SUCCESS;
     * }
     * </pre>
     * @param fields - fields of the type
     * @return - {@link MethodSpec}
     */
    static MethodSpec getValidateMethodSpec(List<Field> fields) {
        MethodSpec.Builder validationMethodBuilder =
                MethodSpec.methodBuilder(Constants.VALIDATE)
                        .addJavadoc("Validates request object\n")
                        .addJavadoc("\n@return {@link ValidationResult}\n")
                        .returns(ValidationResult.class)
                        .addModifiers(Modifier.PUBLIC);

        for (Field field : fields) {
            if (field.required) {
                // glue in validation for required fields
                validationMethodBuilder.addCode("if ($N == null) ", field.name);
                validationMethodBuilder.addCode("{ new ValidationResult(false, \"$N cannot be null\");}\n",
                        field.name);
            }

            if (field.validation != null) {
                //go through each type of validation

                if (field.validation.length != null) {
                    if (field.type.equals("String")) {
                        if (field.validation.length.min > 0) {
                            // has length restriction
                            validationMethodBuilder.addCode("if ($T.isEmpty($N)) ", StringUtils.class, field.name);
                            validationMethodBuilder.addCode("{ new ValidationResult(false, \"$N cannot be empty\");}\n",
                                    field.name);
                        }

                        if (field.validation.length.max > 0) {
                            validationMethodBuilder.addCode("if ($N.length() > $L) ", field.name, field.validation.length.max);
                            validationMethodBuilder.addCode("{ new ValidationResult(false, \"length of $N exceeds the max allowed length of $L\");}\n",
                                    field.name, field.validation.length.max);
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
     * @param type - field type
     * @return - {@link MethodSpec}
     */
    static MethodSpec getSetMethodSpec(String name, String type) {
        return MethodSpec.methodBuilder(RestGeneratorHelper.getSetMethodName(name))
                .addModifiers(Modifier.PUBLIC)
                .addParameter(typeMap.getTypeName(type), name)
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
     * @param type - field type
     * @return - {@link MethodSpec}
     */
    static MethodSpec getGetMethodSpec(String name, String type) {
        return MethodSpec.methodBuilder(Constants.GET_METHOD_PREFIX + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, name))
                .addModifiers(Modifier.PUBLIC)
                .returns(typeMap.getTypeName(type))
                .addStatement("return $N", name)
                .build();
    }
}
