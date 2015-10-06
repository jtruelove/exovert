package com.cyngn.exovert.generate.server.rest;

import com.cyngn.exovert.generate.server.rest.types.Api;
import com.cyngn.exovert.generate.server.rest.utils.Constants;
import com.cyngn.exovert.generate.server.rest.utils.RestGeneratorHelper;
import com.cyngn.vertx.client.ServiceClient;
import com.cyngn.vertx.web.JsonUtil;
import com.google.common.base.Preconditions;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.Modifier;

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

        this.context.client = true;

        // generate common types between server and client
        commonRestGenerator.generate(spec);

        commonRestGenerator.generateClassFromTypespec(
                commonRestGenerator.getPackageNamespace(context, RestGeneratorHelper.getApiNamespace(spec.namespace)),
                getServiceClientSpec(spec));
    }

    /**
     * Generates type spec for service client
     *
     * @param spec - Interface specification
     * @return {@link TypeSpec} for the service client
     */
    public TypeSpec getServiceClientSpec(InterfaceSpec spec) {
        Preconditions.checkArgument(spec != null, "spec == null");
        Preconditions.checkArgument(spec.name != null, "Name field missing in specification");

        TypeSpec.Builder serviceClientBuilder = TypeSpec.classBuilder(RestGeneratorHelper.getServiceClientName(spec.name))
                .addModifiers(Modifier.PUBLIC);

        // add field
        serviceClientBuilder.addField(FieldSpec.builder(ServiceClient.class, Constants.SERVICE_CLIENT_FIELD_NAME,
                Modifier.PROTECTED).build());

        // add constructor
        MethodSpec.Builder constructorSpec = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);

        // add service client for now
        constructorSpec.addParameter(ServiceClient.class, Constants.SERVICE_CLIENT_FIELD_NAME)
                .addStatement("this.$1N = $1N", Constants.SERVICE_CLIENT_FIELD_NAME);

        serviceClientBuilder.addMethod(constructorSpec.build());

        for (Api api : spec.apis) {
            // for each api we generate methods
            MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder(
                    RestGeneratorHelper.getCallApiMethodName(api.name));

            methodSpecBuilder.addModifiers(Modifier.PUBLIC);
            methodSpecBuilder.addJavadoc(api.documentation + "\n");

            if (api.request != null) {
                methodSpecBuilder.addParameter(ClassName.get(
                        commonRestGenerator.getPackageNamespace(context, RestGeneratorHelper.getTypesNamespace(spec.namespace)),
                        RestGeneratorHelper.getRequestObjectName(api.name))
                        , "request");

            }
            methodSpecBuilder.addParameter(ParameterizedTypeName.get(Handler.class, HttpClientResponse.class),
                    "responseHandler");

            methodSpecBuilder.addParameter(ParameterizedTypeName.get(Handler.class, Throwable.class),
                    "exceptionHandler");

            if (api.request != null) {
                methodSpecBuilder.addStatement("byte[] payload = $T.getJsonForObject(request).getBytes()", JsonUtil.class);
            }

            // api.path will vary depending on httpmethod
            String httpMethod = RestGeneratorHelper.getHttpMethod(api.httpMethod);

            if (httpMethod.equals(Constants.HTTP_METHOD_GET)
                    || httpMethod.equals(Constants.HTTP_METHOD_DELETE) ) {
                if (api.request != null) {
                    methodSpecBuilder.addStatement("long timeout = serviceClient.getTimeout($S)", api.name);
                    methodSpecBuilder.beginControlFlow(" if (timeout > 0L)");
                    methodSpecBuilder.addStatement("serviceClient.call($T.$L, $S + $L, payload, timeout, responseHandler, exceptionHandler)",
                            HttpMethod.class, httpMethod, api.path, "request.getQueryString()");
                    methodSpecBuilder.nextControlFlow("else");
                    methodSpecBuilder.addStatement("serviceClient.call($T.$L, $S + $L, payload, responseHandler, exceptionHandler)",
                            HttpMethod.class, httpMethod, api.path, "request.getQueryString()");
                    methodSpecBuilder.endControlFlow();
                } else {
                    methodSpecBuilder.addStatement("long timeout = serviceClient.getTimeout($S)", api.name);
                    methodSpecBuilder.beginControlFlow(" if (timeout > 0L)");
                    methodSpecBuilder.addStatement("serviceClient.call($T.$L, $S + $L, timeout, responseHandler, exceptionHandler)",
                            HttpMethod.class, httpMethod, api.path, "request.getQueryString()");
                    methodSpecBuilder.nextControlFlow("else");
                    methodSpecBuilder.addStatement("serviceClient.call($T.$L, $S + $L, responseHandler, exceptionHandler)",
                            HttpMethod.class, httpMethod, api.path, "request.getQueryString()");
                    methodSpecBuilder.endControlFlow();
                }
            } else {
                if (api.request != null) {
                    methodSpecBuilder.addStatement("long timeout = serviceClient.getTimeout($S)", api.name);
                    methodSpecBuilder.beginControlFlow(" if (timeout > 0L)");
                    methodSpecBuilder.addStatement("serviceClient.call($T.$L, $S, payload, timeout, responseHandler, exceptionHandler)",
                            HttpMethod.class, httpMethod, api.path);
                    methodSpecBuilder.nextControlFlow("else");
                    methodSpecBuilder.addStatement("serviceClient.call($T.$L, $S, payload, responseHandler, exceptionHandler)",
                            HttpMethod.class, httpMethod, api.path);
                    methodSpecBuilder.endControlFlow();
                } else {
                    methodSpecBuilder.addStatement("long timeout = serviceClient.getTimeout($S)", api.name);
                    methodSpecBuilder.beginControlFlow(" if (timeout > 0L)");
                    methodSpecBuilder.addStatement("serviceClient.call($T.$L, $S, timeout, responseHandler, exceptionHandler)",
                            HttpMethod.class, httpMethod, api.path);
                    methodSpecBuilder.nextControlFlow("else");
                    methodSpecBuilder.addStatement("serviceClient.call($T.$L, $S, responseHandler, exceptionHandler)",
                            HttpMethod.class, httpMethod, api.path);
                    methodSpecBuilder.endControlFlow();
                }
            }
            serviceClientBuilder.addMethod(methodSpecBuilder.build());
        }

        return serviceClientBuilder.build();
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
