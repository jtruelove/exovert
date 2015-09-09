package com.cyngn.exovert.generate.server;

import com.cyngn.exovert.generate.server.types.Api;
import com.cyngn.exovert.generate.server.types.Field;
import com.cyngn.vertx.web.JsonUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.CaseFormat;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import io.vertx.core.json.DecodeException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

/**
 * Generates Server interface
 *
 * Interface includes
 *    API classes,
 *    Request, Response classes (with setters, getters and validations)
 *
 * @author asarda@cyngn.com (Ajay Sarda) 9/8/15.
 */
public class InterfaceGenerator {
    private static final Logger logger = LoggerFactory.getLogger(InterfaceGenerator.class);

    // directory constants
    public static final String GENERATED_SRC_DIRECTORY = "generated-src";
    public static final String BUILD_DIRECTORY = "build";

    // class name constants
    public static final String REQUEST_CLASS_SUFFIX = "Request";
    public static final String RESPONSE_CLASS_SUFFIX = "Response";

    // method name constants
    public static final String VALIDATE = "validate";
    public static final String SET_METHOD_PREFIX = "set";
    public static final String GET_METHOD_PREFIX = "get";

    private static final TypeMap typeMap = TypeMap.create();

    public static void main(String[] args) throws Exception {
        // expects api.json in current working directory
        InterfaceGenerator.generate();
    }

    public static void generate() throws Exception {
        InterfaceSpec spec = loadSpecFromFile("api.json");

        for (Api api : spec.apis) {

            // generate request
            TypeSpec typeSpec = getTypeSpec(CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, api.name + REQUEST_CLASS_SUFFIX),
                    api.request.fields, true);

            JavaFile javaFile = JavaFile.builder(spec.namespace, typeSpec)
                    .build();

            javaFile.writeTo(getGeneratedSourceDirectoryPath());

            // generate response
            typeSpec = getTypeSpec(CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, api.name + RESPONSE_CLASS_SUFFIX),
                    api.response.fields, false);


            javaFile = JavaFile.builder(spec.namespace, typeSpec)
                    .build();

            javaFile.writeTo(getGeneratedSourceDirectoryPath());

            // TODO: generate apis in next iteration
        }
    }

    private static Path getGeneratedSourceDirectoryPath() {
        return FileSystems.getDefault().getPath(BUILD_DIRECTORY, GENERATED_SRC_DIRECTORY);
    }

    private static InterfaceSpec loadSpecFromFile(String filename) throws Exception {
        InterfaceSpec spec;
        try (Scanner scanner = new Scanner(new File(filename)).useDelimiter("\\A")) {
            String sconf = scanner.next();
            try {
                spec = JsonUtil.parseJsonToObject(sconf, InterfaceSpec.class);
                logger.info("Successfully loaded specification file " + filename);
            } catch (DecodeException e) {
                logger.error("Api definition file " + sconf + " does not contain a valid JSON object");
                throw e;
            }
        } catch (FileNotFoundException e) {
            logger.error("No api definition file found", e);
            throw e;
        }
        return spec;
    }

    /**
     * Generates {@link TypeSpec} for request and response objects.
     *
     * Glues in validations for request objects.
     *
     * @param name - type name
     * @param fields - fields of the type
     * @param requestType - is it request object type
     * @return - {@link TypeSpec}
     */
    private static TypeSpec getTypeSpec(String name, List<Field> fields, boolean requestType) {

        TypeSpec.Builder builder = TypeSpec.classBuilder(name)
                .addModifiers(Modifier.PUBLIC);

        for (Field field : fields) {
            FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(typeMap.getTypeName(field.type), field.name)
                    .addModifiers(Modifier.PRIVATE)
                    .addAnnotation(AnnotationSpec.builder(JsonProperty.class)
                            .addMember("value", "$S", CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.name)).build());

            if (!field.required) {
                fieldSpecBuilder.addAnnotation(JsonIgnore.class);
            }

            builder.addField(fieldSpecBuilder.build());
            builder.addMethod(getSetMethodSpec(field.name, field.type));
            builder.addMethod(getGetMethodSpec(field.name, field.type));
        }

        if (requestType) {
            builder.addMethod(getValidateMethodSpec(fields));
        }
        return builder.build();
    }

    /**
     * Generates {@link MethodSpec} for validate method
     * @param fields - fields of the type
     * @return - {@link MethodSpec}
     */
    private static MethodSpec getValidateMethodSpec(List<Field> fields) {
        MethodSpec.Builder validationMethodBuilder =
                MethodSpec.methodBuilder(VALIDATE)
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

    private static MethodSpec getSetMethodSpec(String name, String type) {
        return MethodSpec.methodBuilder(SET_METHOD_PREFIX + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, name))
                .addModifiers(Modifier.PUBLIC)
                .addParameter(typeMap.getTypeName(type), name)
                .addStatement("this.$N = $N", name, name)
                .build();
    }

    private static MethodSpec getGetMethodSpec(String name, String type) {
        return MethodSpec.methodBuilder(GET_METHOD_PREFIX + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, name))
                .addModifiers(Modifier.PUBLIC)
                .returns(typeMap.getTypeName(type))
                .addStatement("return $N", name)
                .build();
    }
}
