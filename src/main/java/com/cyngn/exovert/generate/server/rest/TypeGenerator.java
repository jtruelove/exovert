package com.cyngn.exovert.generate.server.rest;

import com.cyngn.exovert.generate.server.rest.types.ClassType;
import com.cyngn.exovert.generate.server.rest.types.DataTypeGroup;
import com.cyngn.exovert.generate.server.rest.types.EnumType;
import com.cyngn.exovert.generate.server.rest.utils.Constants;
import com.cyngn.exovert.generate.server.rest.utils.RestGeneratorHelper;
import com.cyngn.exovert.util.GeneratorHelper;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Generates the types.
 *
 * @author asarda@cyngn.com (Ajay Sarda) 9/17/15.
 */
public class TypeGenerator {
    private static final Logger logger = LoggerFactory.getLogger(TypeGenerator.class);
    private GenerationContext context;
    private ClassGenerator classGenerator;
    private MethodGenerator methodGenerator;

    public TypeGenerator(GenerationContext context) {
        this.context = context;
        this.methodGenerator = new MethodGenerator(this.context);
        this.classGenerator = new ClassGenerator(this.context, this.methodGenerator);
    }

    /**
     * Generates Types
     *
     * @throws Exception on generation failure
     */
    public void generate() throws Exception {
        logger.info("Generating the types...");

        DataTypeSpec spec = RestGeneratorHelper.loadTypeSpecFromFile(context.specFilePath);

        // generate for data type group
        for (DataTypeGroup dataTypeGroup : spec.dataTypeGroups) {
            // recreate the type map on every group to avoid collision across packages.
            context.typeMap = TypeMap.create();

            // generate enums
            if (dataTypeGroup.enumTypes != null) {
                for (EnumType enumType : dataTypeGroup.enumTypes) {

                    TypeSpec typeSpec = classGenerator
                            .getEnumTypeSpec(RestGeneratorHelper.getTypesNamespace(dataTypeGroup.namespace), enumType);

                    generateClassFromTypespec(RestGeneratorHelper.getTypesNamespace(dataTypeGroup.namespace), typeSpec);
                }
            }

            // generate class types
            if (dataTypeGroup.classTypes != null) {
                for (ClassType classType : dataTypeGroup.classTypes) {
                    TypeSpec typeSpec =
                            classGenerator.getTypeSpecBuilder(
                                    RestGeneratorHelper.getTypesNamespace(dataTypeGroup.namespace),
                                    RestGeneratorHelper.getTypeName(classType.name, context.typeMap),
                                    classType.fields, classType.immutable, classType.jsonAnnotations)
                                    .addJavadoc(GeneratorHelper.getJavaDocHeader(classType.documentation)).build();

                    generateClassFromTypespec(RestGeneratorHelper.getTypesNamespace(dataTypeGroup.namespace), typeSpec);
                }
            }
        }
    }

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

    /**
     * Get the instance to {@link RestServerGenerator.Builder}
     *
     * @return {@link RestServerGenerator.Builder}
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Fluent Builder class to build {@link RestServerGenerator}
     */
    public static class Builder {
        private GenerationContext context;

        public Builder() {
            context = new GenerationContext();
        }

        public Builder withIsPreview(boolean isPreview) {
            context.preview = isPreview;
            return this;
        }

        public Builder withOutputDirectory(String outputDirectory) {
            context.outputDirectory = outputDirectory;
            return this;
        }

        public Builder withSpecFilePath(String specFilePath) {
            context.specFilePath = specFilePath;
            return this;
        }

        public TypeGenerator build() {
            return new TypeGenerator(context);
        }
    }
}
