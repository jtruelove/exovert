package com.cyngn.exovert.generate.server;

import com.cyngn.exovert.generate.server.types.Api;
import com.cyngn.exovert.generate.server.utils.Constants;
import com.cyngn.exovert.generate.server.utils.RestGeneratorHelper;
import com.cyngn.vertx.web.JsonUtil;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import io.vertx.core.json.DecodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
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

    public static void main(String[] args) throws Exception {
        // expects api.json in current working directory
        InterfaceGenerator.generate();
    }

    /**
     * Generates Request, Response and API classes.
     *
     * @throws Exception
     */
    public static void generate() throws Exception {
        InterfaceSpec spec = loadSpecFromFile("api.json");

        for (Api api : spec.apis) {
            // generate request
            TypeSpec typeSpec = InterfaceClassGenerator.getRequestTypeSpec(RestGeneratorHelper.getRequestObjectName(api.name),
                    api.request.fields);

            JavaFile javaFile = JavaFile.builder(RestGeneratorHelper.getTypesNamespace(spec.namespace), typeSpec)
                    .indent(Constants.INDENTATION_SPACES).build();
            javaFile.writeTo(RestGeneratorHelper.getGeneratedSourceDirectoryPath());

            // for APIs with void response, do not generate response objects
            if(api.response != null) {
                // generate response
                typeSpec = InterfaceClassGenerator.getResponseTypeSpec(RestGeneratorHelper.getResponseObjectName(api.name),
                        api.response.fields);

                javaFile = JavaFile.builder(RestGeneratorHelper.getTypesNamespace(spec.namespace), typeSpec)
                        .indent(Constants.INDENTATION_SPACES).build();
                javaFile.writeTo(RestGeneratorHelper.getGeneratedSourceDirectoryPath());
            }

            // generate api base classes.
            javaFile = JavaFile.builder(RestGeneratorHelper.getApiNamespace(spec.namespace), InterfaceClassGenerator.getApiTypeSpec(api, spec.namespace))
                    .indent(Constants.INDENTATION_SPACES).build();
            javaFile.writeTo(RestGeneratorHelper.getGeneratedSourceDirectoryPath());
        }
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

}
