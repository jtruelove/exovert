package com.cyngn.exovert.generate.server;

import com.cyngn.exovert.generate.server.types.Api;
import com.cyngn.exovert.generate.server.types.Field;
import com.cyngn.exovert.generate.server.utils.Constants;
import com.cyngn.exovert.generate.server.utils.RestGeneratorHelper;
import com.cyngn.exovert.util.GeneratorHelper;
import com.cyngn.exovert.util.MetaData;
import com.cyngn.vertx.web.RestApi;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.CaseFormat;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeSpec;
import io.vertx.core.http.HttpMethod;

import javax.lang.model.element.Modifier;
import java.util.List;

/**
 * Generates Server API classes, Request, Response classes
 * (with setters, getters and validations)
 *
 * @author asarda@cyngn.com (Ajay Sarda) 9/11/15.
 */
public class InterfaceClassGenerator {

    private static final TypeMap typeMap = TypeMap.create();

    /**
     * Generates {@link TypeSpec} for API class
     *
     * <p>
     * Generated code looks like
     * </p>
     * <pre>
     * public abstract class AbstractGetApi implements RestApi {
     *     private static final Logger logger = LoggerFactory.getLogger(AbstractGetApi.class);
     *     private static final String GET_API_PATH = "/get";
     *     private final RestApi.RestApiDescriptor[] supportedApi =  {
     *         new RestApi.RestApiDescriptor(HttpMethod.GET, GET_API_PATH, this::handleGet),
     *     };
     *
     *    ...... remaining methods.
     *    public void handleGet(final RoutingContext context) {...}
     *
     *    private final void validate(final HttpServerRequest request, final GetRequest getRequest) {...}
     *
     *    public abstract void process(final GetRequest request);
     *
     *    @Override
     *    public RestApi.RestApiDescriptor[] supportedApi() {
     *        return supportedApi;
     *    }
     * }
     * </pre>
     *
     * @param api       - api Object
     * @param namespace - package namespace
     * @return - {@link TypeSpec}
     */
    static TypeSpec getApiTypeSpec(Api api, String namespace) {
        TypeSpec.Builder apiBuilder = TypeSpec.classBuilder(RestGeneratorHelper.getApiName(api.name))
                .addModifiers(Modifier.PUBLIC).addModifiers(Modifier.ABSTRACT);

        apiBuilder.addJavadoc(GeneratorHelper.getJavaDocHeader(api.documentation));
        apiBuilder.addField(GeneratorHelper.getLogger(RestGeneratorHelper.getApiNamespace(namespace), RestGeneratorHelper.getApiName(api.name)));
        apiBuilder.addSuperinterface(RestApi.class);

        addMemberVars(api, apiBuilder);

        if (api.httpMethod.equalsIgnoreCase(Constants.HTTP_METHOD_POST)) {
            apiBuilder.addMethod(InterfaceMethodGenerator.getHandlePostMethodSpec(api, namespace));
        } else if (api.httpMethod.equalsIgnoreCase(Constants.HTTP_METHOD_GET)) {
            apiBuilder.addMethod(InterfaceMethodGenerator.getHandleGetMethodSpec(api, namespace));
        } else if (api.httpMethod.equalsIgnoreCase(Constants.HTTP_METHOD_DELETE)) {
            apiBuilder.addMethod(InterfaceMethodGenerator.getHandleDeleteMethodSpec(api, namespace));
        }

        apiBuilder.addMethod(InterfaceMethodGenerator.getValidateMethodSpec(api, namespace));
        apiBuilder.addMethod(InterfaceMethodGenerator.getProcessMethodSpec(api, namespace));
        apiBuilder.addMethod(InterfaceMethodGenerator.getSupportedApi());

        return apiBuilder.build();
    }

    /**
     * Generates {@link com.squareup.javapoet.TypeSpec.Builder} with setter and getters
     *
     * @param name - type name
     * @param fields - fields of the type
     * @return - {@link TypeSpec.Builder}
     */
    private static TypeSpec.Builder getTypeSpecBuilder(String name, List<Field> fields) {
        TypeSpec.Builder builder = TypeSpec.classBuilder(name)
                .addModifiers(Modifier.PUBLIC);

        for (Field field : fields) {
            FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(typeMap.getTypeName(field.type), RestGeneratorHelper.getFieldName(field.name))
                    .addModifiers(Modifier.PRIVATE)
                    .addAnnotation(AnnotationSpec.builder(JsonProperty.class)
                            .addMember("value", "$S", CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.name.toLowerCase())).build());

            if (!field.required) {
                fieldSpecBuilder.addAnnotation(JsonIgnore.class);
            }

            builder.addField(fieldSpecBuilder.build());
            builder.addMethod(InterfaceMethodGenerator.getSetMethodSpec(field.name, field.type));
            builder.addMethod(InterfaceMethodGenerator.getGetMethodSpec(field.name, field.type));
        }

       return builder;
    }

    /**
     * Generates {@link TypeSpec} for request type.
     * <p>
     * Glues in validations.
     *
     * <p>
     * Generated code looks like
     * </p>
     * <pre>
     * public class GetRequest {
     *     @JsonProperty("name")
     *     private String name;
     *
     *     public void setName(String name) {
     *         this.name = name;
     *     }
     *
     *     public String getName() {
     *          return name;
     *     }
     *
     *     /** Validates request object
     *      *
     *      * @return {@link ValidationResult}
     *      *\/
     *
     *     public ValidationResult validate() {
     *         if (name == null) { new ValidationResult(false, "name cannot be null");}
     *         if (StringUtils.isEmpty(name)) { new ValidationResult(false, "name cannot be empty");}
     *         if (name.length() > 1000) { new ValidationResult(false, "length of name exceeds the max allowed length of 1000");}
     *         return ValidationResult.SUCCESS;
     *     }
     * }
     * </pre>
     *
     * @param name   - type name
     * @param fields - fields of the type
     * @return - {@link TypeSpec}
     */
    static TypeSpec getRequestTypeSpec(String name, List<Field> fields) {
        return getTypeSpecBuilder(name, fields)
                .addMethod(InterfaceMethodGenerator.getValidateMethodSpec(fields))
                .addJavadoc(GeneratorHelper.getJavaDocHeader("Request type for " + name + " Api"))
                .build();
    }

    /**
     * Generates {@link TypeSpec} for response type.
     *
     * <p>
     * Generated code looks like
     * </p>
     * <pre>
     * public class GetResponse {
     *     @JsonProperty("name")
     *     private String name;
     *     public void setName(String name) {
     *         this.name = name;
     *     }
     *
     *     public String getName() {
     *         return name;
     *     }
     * }
     * @param name   - type name
     * @param fields - fields of the type
     * @return - {@link TypeSpec}
     */
    static TypeSpec getResponseTypeSpec(String name, List<Field> fields) {
        return getTypeSpecBuilder(name, fields)
                .addJavadoc(GeneratorHelper.getJavaDocHeader("Response type for " + name + " Api"))
                .build();
    }

    /**
     * Adds member variables for Api
     *
     * @param api     - api Object
     * @param builder - TypeSpec builder for api
     */
    static void addMemberVars(Api api, TypeSpec.Builder builder) {
        String apiConstant = api.name.toUpperCase() + Constants.API_PATH;

        builder.addField(FieldSpec.builder(String.class, apiConstant, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", api.path).build());

        CodeBlock block = CodeBlock.builder().beginControlFlow("")
                .add("new RestApi.RestApiDescriptor($T.$L, $L, this::$L),\n", HttpMethod.class,
                        api.httpMethod.toUpperCase(), apiConstant, RestGeneratorHelper.getHandlerName(api.httpMethod))
                .unindent().add("}")
                .build();

        builder.addField(FieldSpec.builder(RestApi.RestApiDescriptor[].class, Constants.SUPPORTED_API_FIELD, Modifier.PRIVATE,
                Modifier.FINAL)
                .initializer(block)
                .build());
    }

}
