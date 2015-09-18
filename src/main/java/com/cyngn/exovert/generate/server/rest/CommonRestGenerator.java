package com.cyngn.exovert.generate.server.rest;

import com.cyngn.exovert.generate.server.rest.types.Api;
import com.cyngn.exovert.generate.server.rest.types.ClassType;
import com.cyngn.exovert.generate.server.rest.types.EnumType;
import com.cyngn.exovert.generate.server.rest.utils.Constants;
import com.cyngn.exovert.generate.server.rest.utils.RestGeneratorHelper;
import com.cyngn.exovert.util.GeneratorHelper;
import com.google.common.base.Preconditions;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Generator to generate common types between
 * Server and Client
 *
 * @author asarda@cyngn.com (Ajay Sarda) 9/17/15.
 */
class CommonRestGenerator {
    private static final Logger logger = LoggerFactory.getLogger(CommonRestGenerator.class);
    private GenerationContext context;
    private ClassGenerator classGenerator;
    private MethodGenerator methodGenerator;

    public CommonRestGenerator(GenerationContext context) {
        this.context = context;
        this.context.typeMap = TypeMap.create();
        this.methodGenerator = new MethodGenerator(this.context);
        this.classGenerator = new ClassGenerator(this.context, this.methodGenerator);
    }

    /**
     * Generates Request, Response classes, types
     *
     * @throws Exception on generation failure
     */
    public void generate(InterfaceSpec spec) throws Exception {
        Preconditions.checkArgument(spec != null, "Interface specification cannot be null");

        // generate types
        if (spec.dataTypes != null) {
            if (spec.dataTypes.enumTypes != null) {
                for (EnumType enumType : spec.dataTypes.enumTypes) {

                    TypeSpec typeSpec = classGenerator
                            .getEnumTypeSpec(RestGeneratorHelper.getTypesNamespace(spec.namespace), enumType);

                    generateClassFromTypespec(RestGeneratorHelper.getTypesNamespace(spec.namespace), typeSpec);
                }
            }

            if (spec.dataTypes.classTypes != null) {
                for (ClassType classType : spec.dataTypes.classTypes) {
                    TypeSpec typeSpec =
                            classGenerator.getTypeSpecBuilder(
                                    RestGeneratorHelper.getTypesNamespace(spec.namespace),
                                    RestGeneratorHelper.getTypeName(classType.name),
                                    classType.fields, classType.immutable, classType.jsonAnnotations)
                                    .addJavadoc(GeneratorHelper.getJavaDocHeader(classType.documentation)).build();

                    generateClassFromTypespec(RestGeneratorHelper.getTypesNamespace(spec.namespace), typeSpec);
                }
            }
        }

        for (Api api : spec.apis) {
            // generate request
            TypeSpec typeSpec = classGenerator.getRequestTypeSpec(
                    RestGeneratorHelper.getTypesNamespace(spec.namespace),
                    RestGeneratorHelper.getRequestObjectName(api.name),
                    api.request.fields);

            generateClassFromTypespec(RestGeneratorHelper.getTypesNamespace(spec.namespace), typeSpec);

            // for APIs with void response, do not generate response objects
            if(api.response != null) {
                // generate response
                typeSpec = classGenerator.getResponseTypeSpec(
                        RestGeneratorHelper.getTypesNamespace(spec.namespace),
                        RestGeneratorHelper.getResponseObjectName(api.name),
                        api.response.fields);

                generateClassFromTypespec(RestGeneratorHelper.getTypesNamespace(spec.namespace), typeSpec);
            }
        }
    }

    /**
     * Dumps the class from TypeSpec
     *
     * @param namespace - package namespace
     * @param typeSpec - TypeSpec
     * @throws IOException - on write failure.
     */
    public void generateClassFromTypespec(String namespace, TypeSpec typeSpec) throws IOException {
        JavaFile javaFile = JavaFile.builder(namespace, typeSpec)
                .indent(Constants.INDENTATION_SPACES).build();

        logger.info(String.format("Generating package name:%s, class:%s", namespace, typeSpec.name));

        if (this.context.preview) {
            javaFile.writeTo(System.out);
        } else {
            javaFile.writeTo(RestGeneratorHelper.getDirectoryPath(context.outputDirectory));
        }
    }
}
