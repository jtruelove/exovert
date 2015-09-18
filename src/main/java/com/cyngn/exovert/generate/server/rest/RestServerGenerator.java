package com.cyngn.exovert.generate.server.rest;

import com.cyngn.exovert.generate.server.rest.types.Api;
import com.cyngn.exovert.generate.server.rest.utils.RestGeneratorHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates Server interface
 *
 * Interface includes
 *    API classes,
 *    Request, Response classes (with setters, getters and validations)
 *
 * @author asarda@cyngn.com (Ajay Sarda) 9/8/15.
 */
public class RestServerGenerator {
    private static final Logger logger = LoggerFactory.getLogger(RestServerGenerator.class);

    private GenerationContext context;
    private ClassGenerator classGenerator;
    private MethodGenerator methodGenerator;
    private CommonRestGenerator commonRestGenerator;

    private RestServerGenerator(GenerationContext context) {
        this.context = context;
        this.context.typeMap = TypeMap.create();
        this.methodGenerator = new MethodGenerator(this.context);
        this.classGenerator = new ClassGenerator(this.context, this.methodGenerator);
        this.commonRestGenerator = new CommonRestGenerator(this.context);
    }

    /**
     * Generates Request, Response and API classes.
     *
     * @throws Exception on generation failure
     */
    public void generate() throws Exception {
        logger.info("Generating the REST server ...");

        InterfaceSpec spec = RestGeneratorHelper.loadSpecFromFile(context.specFilePath);

        // generate common types between server and client
        commonRestGenerator.generate(spec);

        for (Api api : spec.apis) {
            // add api class for server
            commonRestGenerator.generateClassFromTypespec(
                    RestGeneratorHelper.getApiNamespace(spec.namespace),
                    classGenerator.getApiTypeSpec(api, spec.namespace));
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

        public RestServerGenerator build() {
            return new RestServerGenerator(context);
        }
    }
}
