package com.cyngn.exovert.generate.server.rest;

import com.cyngn.exovert.generate.server.rest.utils.RestGeneratorHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates the REST Client.
 *
 * Generated artifacts include Request, Response types.
 *
 * @author asarda@cyngn.com (Ajay Sarda) 9/16/15.
 */
public class RestClientGenerator {

    private static final Logger logger = LoggerFactory.getLogger(RestClientGenerator.class);
    private GenerationContext context;
    private CommonRestGenerator commonRestGenerator;

    private RestClientGenerator(GenerationContext context) {
        this.context = context;
        this.context.typeMap = TypeMap.create();
        this.commonRestGenerator = new CommonRestGenerator(this.context);
    }

    /**
     * Generates Request, Response classes, types
     *
     * @throws Exception on generation failure
     */
    public void generate() throws Exception {
        logger.info("Generating the REST client...");

        InterfaceSpec spec = RestGeneratorHelper.loadSpecFromFile(context.specFilePath);

        // generate common types between server and client
        commonRestGenerator.generate(spec);
    }

    /**
     * Get the instance to {@link RestClientGenerator.Builder}
     *
     * @return {@link RestClientGenerator.Builder}
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Fluent Builder class to build {@link RestClientGenerator}
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

        public RestClientGenerator build() {
            return new RestClientGenerator(context);
        }
    }
}
