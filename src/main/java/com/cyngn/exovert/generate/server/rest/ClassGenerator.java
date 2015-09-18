package com.cyngn.exovert.generate.server.rest;

import com.cyngn.exovert.generate.server.rest.types.Api;
import com.cyngn.exovert.generate.server.rest.types.EnumType;
import com.cyngn.exovert.generate.server.rest.types.EnumValue;
import com.cyngn.exovert.generate.server.rest.types.Field;
import com.cyngn.exovert.generate.server.rest.utils.Constants;
import com.cyngn.exovert.generate.server.rest.utils.RestGeneratorHelper;
import com.cyngn.exovert.util.GeneratorHelper;
import com.cyngn.vertx.web.RestApi;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.CaseFormat;
import com.google.common.base.Preconditions;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.vertx.core.http.HttpMethod;
import org.apache.commons.lang.StringUtils;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates Server API classes, Request, Response classes
 * (with setters, getters and validations)
 *
 * @author asarda@cyngn.com (Ajay Sarda) 9/11/15.
 */
public class ClassGenerator {
    private GenerationContext context;
    private MethodGenerator methodGenerator;

    public ClassGenerator(GenerationContext context, MethodGenerator methodGenerator) {
        this.context = context;
        this.methodGenerator = methodGenerator;
    }

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
    TypeSpec getApiTypeSpec(Api api, String namespace) {
        Preconditions.checkArgument(api != null, "api == null");
        Preconditions.checkArgument(StringUtils.isNotEmpty(namespace), "namespace cannot be empty or null");

        TypeSpec.Builder apiBuilder = TypeSpec.classBuilder(RestGeneratorHelper.getApiName(api.name))
                .addModifiers(Modifier.PUBLIC).addModifiers(Modifier.ABSTRACT);

        apiBuilder.addJavadoc(GeneratorHelper.getJavaDocHeader(api.documentation));
        apiBuilder.addField(GeneratorHelper.getLogger(RestGeneratorHelper.getApiNamespace(namespace), RestGeneratorHelper.getApiName(api.name)));
        apiBuilder.addSuperinterface(RestApi.class);

        addMemberVars(api, apiBuilder);

        if (api.httpMethod.equalsIgnoreCase(Constants.HTTP_METHOD_POST)) {
            apiBuilder.addMethod(methodGenerator.getHandlePostMethodSpec(api, namespace));
        } else if (api.httpMethod.equalsIgnoreCase(Constants.HTTP_METHOD_GET)) {
            apiBuilder.addMethod(methodGenerator.getHandleGetMethodSpec(api, namespace));
        } else if (api.httpMethod.equalsIgnoreCase(Constants.HTTP_METHOD_DELETE)) {
            apiBuilder.addMethod(methodGenerator.getHandleDeleteMethodSpec(api, namespace));
        }

        apiBuilder.addMethod(methodGenerator.getValidateMethodSpec(api, namespace));
        apiBuilder.addMethod(methodGenerator.getProcessMethodSpec(api, namespace));
        apiBuilder.addMethod(methodGenerator.getSupportedApi());

        return apiBuilder.build();
    }

    /**
     * Generates {@link com.squareup.javapoet.TypeSpec.Builder} with setter and getters
     *
     * @param namespace - package namespace of the type
     * @param name - type name
     * @param fields - fields of the type
     * @return - {@link TypeSpec.Builder}
     */
    private TypeSpec.Builder getTypeSpecBuilder(String namespace, String name, List<Field> fields) {
        return getTypeSpecBuilder(namespace, name, fields, false, true);
    }

    /**
     * Generates full blown {@link com.squareup.javapoet.TypeSpec.Builder} with
     *
     * private fields with json annotations if asked for.
     * Empty constructor (for Json serialization)
     * Constructor with all parameters
     * Constructor with Builder object
     *
     * Setters (for mutable types)
     * Getters
     *
     * hashCode()
     * equals()
     * toString()
     *
     * Builder inner type
     *
     * @param namespace - package namespace of the type
     * @param name - type name
     * @param fields - fields of the type
     * @param immutable - whether the type is immutable
     * @param jsonAnnotations - apply json annotations
     * @return - {@link TypeSpec.Builder}
     */
    TypeSpec.Builder getTypeSpecBuilder(String namespace, String name, List<Field> fields,
                                               boolean immutable, boolean jsonAnnotations) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(namespace), "namespace cannot be empty or null");

        //register type object with type map for future reference
        context.typeMap.registerType(name, ClassName.get(namespace, name));

        //register builder type object with type map for future reference
        context.typeMap.registerType(RestGeneratorHelper.getBuilderTypeName(name),
                ClassName.get(namespace, name, Constants.BUILDER_CLASS_NAME));

        TypeSpec.Builder builder = TypeSpec.classBuilder(name).addModifiers(Modifier.PUBLIC);

        // constructor with all parameters
        MethodSpec.Builder constructorSpec = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);

        // constructor with builder
        MethodSpec.Builder constructorWithBuildObjectSpec = MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PRIVATE)
                        .addParameter(context.typeMap.getTypeName(RestGeneratorHelper.getBuilderTypeName(name)), RestGeneratorHelper.getBuilderVariableName());

        List<String> fieldNames = new ArrayList<>();

        for (Field field : fields) {
            String fieldName = RestGeneratorHelper.getFieldName(field.name);
            TypeName fieldTypeName = TypeParser.parse(RestGeneratorHelper.getTypeName(field.type), context.typeMap);

            // field spec
            FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(fieldTypeName, fieldName)
                        .addModifiers(Modifier.PRIVATE);

            // augment json annotations.
            if (jsonAnnotations) {
                fieldSpecBuilder.addAnnotation(AnnotationSpec.builder(JsonProperty.class)
                        .addMember("value", "$S", field.name).build());
                if (!field.required) {
                    fieldSpecBuilder.addAnnotation(JsonIgnore.class);
                }
            }

            // collect the field names for hash code and equals method
            fieldNames.add(fieldName);

            // add field
            builder.addField(fieldSpecBuilder.build());

            /*
             * Add setter method if type is not immutable
             */
            if (!immutable) {
                // add setter for field
                builder.addMethod(methodGenerator.getSetMethodSpec(fieldName, fieldTypeName));
            }

            // add getter for field.
            builder.addMethod(methodGenerator.getGetMethodSpec(fieldName, fieldTypeName));

            // field assignment in constructor
            constructorSpec.addParameter(fieldTypeName, fieldName)
                    .addStatement("this.$N = $N", fieldName, fieldName);

            // field assignment from builder object
            constructorWithBuildObjectSpec
                    .addStatement("this.$N = builder.$N", fieldName, fieldName);
        }

        // add empty constructor for Json serialization
        builder.addMethod(methodGenerator.getEmptyConstructorSpec());

        // add constructor with all parameters
        builder.addMethod(constructorSpec.build());

        // add constructor with all builder
        builder.addMethod(constructorWithBuildObjectSpec.build());

        // add builder to the type
        builder.addType(getBuilderTypeSpecBuilder(name, fields).build());

        // add hashCode method
        builder.addMethod(methodGenerator.getHashCodeSpec(fieldNames));

        // add equals method
        builder.addMethod(methodGenerator.getEqualsCodeSpec(name, fieldNames));

        // add toString method
        builder.addMethod(methodGenerator.getToStringCodeSpec());

        return builder;
    }

    /**
     * Returns the type spec for Builder for class types.
     * <p>
     * Generated code looks like
     * </p>
     * <pre>
     * public static class Builder {
     *     private String name;
     *
     *     public Builder name(String name) {
     *         this.name = name;
     *         return this;
     *     }
     *
     *     public GetRequest build() {
     *         return new GetRequest(this);
     *     }
     * }
     * </pre>
     * @param name
     * @param fields
     * @return
     */
    TypeSpec.Builder getBuilderTypeSpecBuilder(String name, List<Field> fields) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(name), "name cannot be empty or null");

        TypeSpec.Builder builder = TypeSpec.classBuilder(Constants.BUILDER_CLASS_NAME)
                .addModifiers(Modifier.PUBLIC)
                .addModifiers(Modifier.STATIC);

        for (Field field : fields) {
            TypeName fieldTypeName = TypeParser.parse(RestGeneratorHelper.getTypeName(field.type), context.typeMap);
            String fieldName = RestGeneratorHelper.getFieldName(field.name);

            builder.addField(FieldSpec.builder(fieldTypeName, fieldName)
                    .addModifiers(Modifier.PRIVATE).build());

            // add builder method
            MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder(fieldName)
                    .addParameter(fieldTypeName, fieldName)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(context.typeMap.getTypeName(RestGeneratorHelper.getBuilderTypeName(name)))
                    .addStatement("this.$N = $N", fieldName, fieldName)
                    .addStatement("return this");

            builder.addMethod(methodSpecBuilder.build());

        }

        // add build() method
        MethodSpec buildSpec = MethodSpec.methodBuilder("build")
                .addModifiers(Modifier.PUBLIC)
                .returns(context.typeMap.getTypeName(name))
                .addStatement("return new $N(this)", name).build();

        builder.addMethod(buildSpec);

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
    TypeSpec getRequestTypeSpec(String namespace, String name, List<Field> fields) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(namespace), "package namespace cannot be empty or null");
        Preconditions.checkArgument(StringUtils.isNotEmpty(name), "type name cannot be empty or null");

        return getTypeSpecBuilder(namespace, name, fields)
                .addMethod(methodGenerator.getValidateMethodSpec(fields))
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
    TypeSpec getResponseTypeSpec(String namespace, String name, List<Field> fields) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(namespace), "package namespace cannot be empty or null");
        Preconditions.checkArgument(StringUtils.isNotEmpty(name), "type name cannot be empty or null");

        return getTypeSpecBuilder(namespace, name, fields)
                .addJavadoc(GeneratorHelper.getJavaDocHeader("Response type for " + name + " Api"))
                .build();
    }

    /**
     * Adds member variables for Api
     *
     * @param api     - api Object
     * @param builder - DataTypeSpec builder for api
     */
    void addMemberVars(Api api, TypeSpec.Builder builder) {
        Preconditions.checkArgument(api != null, "api == null");
        Preconditions.checkArgument(builder != null, "builder == null");

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

    /**
     * Generates {@link TypeSpec} for Enum type.
     *
     * <p>
     * Generated code looks like
     * </p>
     * <pre>
     * public enum BeerType {
     *     ALCOHOLIC("alcoholic"),
     *
     *     NON_ALCOHOLIC("non_alcoholic");
     *
     *     private final String value;
     *
     *     BeerType(String value) {
     *         this.value = value;
     *     }
     *
     *     public String toString() {
     *         return value;
     *     }
     *
     *    public BeerType fromValue(final String value) throws IllegalArgumentException {
     *         for (BeerType enumValue : BeerType.values()) {
     *             if (enumValue.value.equals(value)) {
     *                 return enumValue;
     *             }
     *         }
     *         throw new IllegalArgumentException("Unknown enum value :" + value);
     *     }
     * }
     * </pre>
     * @param namespace - package namespace
     * @param enumType - Enumerated type
     * @return - {@link TypeSpec}
     */
    TypeSpec getEnumTypeSpec(String namespace, EnumType enumType) {

        Preconditions.checkArgument(StringUtils.isNotEmpty(namespace), "package namespace cannot be empty or null");
        Preconditions.checkArgument(enumType != null, "enumType == null");

        context.typeMap.registerType(RestGeneratorHelper.getTypeName(enumType.name),
                ClassName.get(namespace,
                        RestGeneratorHelper.getTypeName(enumType.name)));

        TypeSpec.Builder enumTypespecBuilder = TypeSpec
                .enumBuilder(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, enumType.name))
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc(GeneratorHelper.getJavaDocHeader(enumType.documentation));

        enumTypespecBuilder.addField(String.class, "value", Modifier.PRIVATE, Modifier.FINAL);

        for (EnumValue enumValue : enumType.values) {
            enumTypespecBuilder.addEnumConstant(
                    enumValue.name.toUpperCase(), TypeSpec.anonymousClassBuilder("$S", enumValue.value).build());
        }

        enumTypespecBuilder.addMethod(MethodSpec.constructorBuilder()
                .addParameter(String.class, "value")
                .addStatement("this.$N = $N", "value", "value").build());

        enumTypespecBuilder.addMethod(MethodSpec.methodBuilder("toString")
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class)
                .addStatement("return value").build());

        // fromValue method
        enumTypespecBuilder.addMethod(MethodSpec.methodBuilder("fromValue")
                .addModifiers(Modifier.PUBLIC)
                .addException(IllegalArgumentException.class)
                .returns(ClassName.get(namespace, CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, enumType.name)))
                .addParameter(String.class, "value", Modifier.FINAL)
                .beginControlFlow("for ($L enumValue : $L.values())",
                        CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, enumType.name),
                        CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, enumType.name))
                .beginControlFlow("if (enumValue.value.equals(value))")
                .addStatement("return enumValue")
                .endControlFlow()
                .endControlFlow()
                .addStatement("throw new IllegalArgumentException(\"Unknown enum value :\" + value)")
                .build());

        return enumTypespecBuilder.build();
    }

}
