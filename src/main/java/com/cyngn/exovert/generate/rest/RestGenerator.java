package com.cyngn.exovert.generate.rest;

import com.cyngn.exovert.generate.entity.EntityGeneratorHelper;
import com.cyngn.exovert.util.Disk;
import com.cyngn.exovert.util.GeneratorHelper;
import com.cyngn.exovert.util.MetaData;
import com.cyngn.vertx.web.HttpHelper;
import com.cyngn.vertx.web.JsonUtil;
import com.cyngn.vertx.web.RestApi;
import com.datastax.driver.core.TableMetadata;
import com.google.common.base.CaseFormat;
import com.squareup.javapoet.*;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang.StringUtils;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author truelove@cyngn.com (Jeremy Truelove) 9/1/15
 */
public class RestGenerator {

    private static final String SUPPORTED_API_FIELD = "supportedApi";
    private static final String PRIMARY_KEY_FIELD = "primaryKey";

    public static void generate(Collection<TableMetadata> tables) throws IOException {
        String namespaceToUse = MetaData.instance.getRestNamespace();

        createUtilClass(namespaceToUse);

        for (TableMetadata table : tables) {
            String tableName = table.getName();
            String rawName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, tableName);
            String name = rawName + "Api";
            String dalName = rawName + "Dal";

            TypeSpec.Builder restBuilder = TypeSpec.classBuilder(name).addModifiers(Modifier.PUBLIC);

            restBuilder.addField(GeneratorHelper.getLogger(namespaceToUse, name));

            restBuilder.addJavadoc(MetaData.getJavaDocHeader("REST Api for Cassandra entity - {@link " +
                    ClassName.get(MetaData.instance.getTableNamespace(), rawName)) + "}");

            restBuilder.addSuperinterface(RestApi.class);
            addMemberVars(tableName, dalName, restBuilder);

            restBuilder.addMethod(getConstructor(dalName, EntityGeneratorHelper.getPrimaryKey(table)));

            addSave(restBuilder, rawName);
            restBuilder.addMethod(getDelete(rawName));
            restBuilder.addMethod(getGet(rawName));
            restBuilder.addMethod(getSupportedApi());

            JavaFile javaFile = JavaFile.builder(namespaceToUse, restBuilder.build()).build();
            Disk.outputFile(javaFile);
        }
    }

    private static void createUtilClass(String namespaceToUse) throws IOException {
        TypeSpec.Builder utilBuilder = TypeSpec.classBuilder("RestUtil").addModifiers(Modifier.PUBLIC)
            .addJavadoc("Central place to put shared functions for REST call processing.\n")
            .addMethod(getIsValidMethod());
        JavaFile javaFile = JavaFile.builder(namespaceToUse, utilBuilder.build()).build();
        Disk.outputFile(javaFile);
    }

    private static MethodSpec getConstructor(String dalName, List<String> primaryKey) {
        MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get(MetaData.instance.getDalNamespace(), dalName), "storage")
                .addStatement("this.storage = $L", "storage")
                .addCode("$L = new $T[]", PRIMARY_KEY_FIELD, String.class).beginControlFlow("");

        boolean first = true;
        for (String key : primaryKey) {
            if(first) {
                builder.addCode("$S", key);
                first = false;
            } else { builder.addCode(",\n$S", key); }
        }
        builder.addCode("\n");
        builder.endControlFlow("");

        return builder.build();
    }


    private static void addMemberVars(String rawClass, String dalName, TypeSpec.Builder builder) {
        String storageNamespace = MetaData.instance.getDalNamespace();
        String apiConstant = rawClass.toUpperCase() + "_API";

        builder.addField(FieldSpec.builder(String.class, apiConstant, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", MetaData.instance.getRestPrefix() + rawClass).build());
        builder.addField(ClassName.get(storageNamespace, dalName), "storage", Modifier.PRIVATE, Modifier.FINAL);

        CodeBlock block = CodeBlock.builder().beginControlFlow("")
                .add("new RestApi.RestApiDescriptor($T.POST, $L, this::save),\n", HttpMethod.class, apiConstant)
                .add("new RestApi.RestApiDescriptor($T.GET, $L, this::get),\n", HttpMethod.class, apiConstant)
                .add("new RestApi.RestApiDescriptor($T.DELETE, $L, this::delete)\n", HttpMethod.class, apiConstant)
                .unindent().add("}")
                .build();

        builder.addField(FieldSpec.builder(RestApi.RestApiDescriptor[].class, SUPPORTED_API_FIELD, Modifier.PRIVATE,
                Modifier.FINAL)
                .initializer(block)
                .build());

        builder.addField(String[].class, "primaryKey", Modifier.FINAL);
    }

    private static void addSave(TypeSpec.Builder builder, String entityName) {
        builder.addMethod(getSave(entityName));
        builder.addMethod(getInternalSave(entityName));
    }

    private static MethodSpec getSave(String entityName) {
        return MethodSpec.methodBuilder("save")
                .addJavadoc(getJavaDoc("Save"), ClassName.get(MetaData.instance.getTableNamespace(), entityName))
                .addParameter(RoutingContext.class, "context")
                .addStatement("$T request = context.request()", HttpServerRequest.class)
                .addModifiers(Modifier.PUBLIC)
                .beginControlFlow("if(request.isEnded())")
                .addStatement("save(context, context.getBody())")
                .nextControlFlow("else")
                .addStatement("request.bodyHandler(buffer -> save(context, buffer))")
                .endControlFlow()
                .build();
    }

    private static MethodSpec getInternalSave(String entityName) {
        ClassName clazz = ClassName.get(MetaData.instance.getTableNamespace(), entityName);

        return MethodSpec.methodBuilder("save")
                .addJavadoc(getJavaDoc("Save") +
                    "\nNOTE: this method is left intentionally package protected to allow you to call it in a different way\n", clazz)
                .addParameter(RoutingContext.class, "context")
                .addParameter(Buffer.class, "body")
                .addStatement("$T entity = $T.parseJsonToObject(body.toString(), $L.class)", clazz, JsonUtil.class, entityName)
                .beginControlFlow("if(entity == null)")
                .addStatement("$T.processErrorResponse($S + body, context.response(), $T.BAD_REQUEST.code())",
                        HttpHelper.class, "Failed to parse body: ", HttpResponseStatus.class)
                .addStatement("return")
                .endControlFlow()
                .addCode("\nstorage.save(entity, result -> ", Consumer.class)
                .beginControlFlow("")
                .beginControlFlow("if(result.succeeded)")
                .addStatement("$T.processResponse(context.response())", HttpHelper.class)
                .nextControlFlow("else if(result.error != null)")
                .addStatement("String error = $S + entity.toString() + $S + result.error.getMessage()", "Could not persist ", ", error: ")
                .addStatement("logger.error($S,  error)", "save - {}")
                .addStatement("$T.processErrorResponse(error, context.response(), $T.INTERNAL_SERVER_ERROR.code())",
                        HttpHelper.class, HttpResponseStatus.class)
                .nextControlFlow("else")
                .addStatement("String error = $S + entity.toString() + $S + result.errorMessage", "Could not persist ", ", error: ")
                .addStatement("logger.error($S,  error)", "save - {}")
                .addStatement("$T.processErrorResponse(error, context.response(), $T.BAD_REQUEST.code())",
                        HttpHelper.class, HttpResponseStatus.class)
                .endControlFlow()
                .endControlFlow(")")
                .build();

    }

    private static MethodSpec getDelete(String entityName) {
        ClassName clazz = ClassName.get(MetaData.instance.getTableNamespace(), entityName);

        return MethodSpec.methodBuilder("delete")
                .addJavadoc(getJavaDoc("Delete"), clazz)
                .addParameter(RoutingContext.class, "context")
                .addModifiers(Modifier.PUBLIC)
                .addCode("// if query params aren't valid a HttpResponseStatus.BAD_REQUEST will be sent with the missing field\n")
                .addStatement("$T queryKey = $T.isValid(context.request(), primaryKey)", Object[].class,
                        ClassName.get(MetaData.instance.getRestNamespace(), "RestUtil"))
                .beginControlFlow("\nif(queryKey != null)")
                .addCode("storage.delete(result -> ", Consumer.class)
                .beginControlFlow("")
                .beginControlFlow("if(result.succeeded)")
                .addStatement("$T.processResponse(context.response())", HttpHelper.class)
                .nextControlFlow("else if (result.error != null)")
                .addStatement("String error = $S + $T.join(queryKey, \",\") + $S + result.error.getMessage()", "Could not delete key: {", StringUtils.class, "}, error: ")
                .addStatement("logger.error($S,  error)", "delete - {}")
                .addStatement("$T.processErrorResponse(error, context.response(), $T.INTERNAL_SERVER_ERROR.code())",
                        HttpHelper.class, HttpResponseStatus.class)
                .nextControlFlow("else")
                .addStatement("String error = $S + StringUtils.join(queryKey, \",\") + $S + result.errorMessage", "Could not delete key: {", "}, error: ")
                .addStatement("logger.error($S, error)", "delete - {}")
                .addStatement("$T.processErrorResponse(error, context.response(), $T.BAD_REQUEST.code())",
                        HttpHelper.class, HttpResponseStatus.class)
                .endControlFlow()
                .endControlFlow(", queryKey)")
                .endControlFlow()
                .build();
    }

    private static MethodSpec getGet(String entityName) {
        ClassName clazz = ClassName.get(MetaData.instance.getTableNamespace(), entityName);

        return MethodSpec.methodBuilder("get")
                .addJavadoc(getJavaDoc("Get"), clazz)
                .addParameter(RoutingContext.class, "context")
                .addModifiers(Modifier.PUBLIC)
                .addCode("// if query params aren't valid a HttpResponseStatus.BAD_REQUEST will be sent with the missing field\n")
                .addStatement("$T queryKey = $T.isValid(context.request(), primaryKey)", Object[].class,
                        ClassName.get(MetaData.instance.getRestNamespace(), "RestUtil"))
                .beginControlFlow("\nif(queryKey != null)")
                .addCode("storage.get(result -> ", Consumer.class)
                .beginControlFlow("")
                .beginControlFlow("if(result.succeeded)")
                .addStatement("$T.processResponse(result.value, context.response())", HttpHelper.class)
                .nextControlFlow("else if(result.error != null)")
                .addStatement("String error = $S + StringUtils.join(queryKey, \",\") + $S + result.error.getMessage()", "Could not get key: {", "}, error: ")
                .addStatement("logger.error($S,  error)", "get - {}")
                .addStatement("$T.processErrorResponse(error, context.response(), $T.INTERNAL_SERVER_ERROR.code())",
                        HttpHelper.class, HttpResponseStatus.class)
                .nextControlFlow("else")
                .addStatement("String error = $S + $T.join(queryKey, \",\") + $S + result.errorMessage", "Could not get key: {", StringUtils.class, "}, error: ")
                .addStatement("logger.error($S, error)", "get - {}")
                .addStatement("$T.processErrorResponse(error, context.response(), $T.BAD_REQUEST.code())",
                        HttpHelper.class, HttpResponseStatus.class)
                .endControlFlow()
                .endControlFlow(", queryKey)")
                .endControlFlow()
                .build();

    }

    private static String getJavaDoc(String action) {
        return action + " a {@link $L} object.\n";
    }

    private static MethodSpec getSupportedApi() {
        return MethodSpec.methodBuilder("supportedApi")
                .addAnnotation(Override.class)
                .addStatement("$L", "return " + SUPPORTED_API_FIELD)
                .returns(TypeName.get(RestApi.RestApiDescriptor[].class))
                .addModifiers(Modifier.PUBLIC)
                .build();
    }

    private static MethodSpec getIsValidMethod() {
        return MethodSpec.methodBuilder("isValid").addModifiers(Modifier.PUBLIC, Modifier.STATIC).returns(Object[].class)
                .addJavadoc("Does the query string of this request contain the full primary key?\n")
                .addParameter(HttpServerRequest.class, "request")
                .addParameter(String[].class, PRIMARY_KEY_FIELD)
                .addStatement("$T queryKey = new Object[$L]", Object[].class, PRIMARY_KEY_FIELD + ".length")
                .addStatement("$T error = null", String.class)
                .beginControlFlow("for(int i = 0; i < $L.length; i++)", PRIMARY_KEY_FIELD)
                .addStatement("$T key = primaryKey[i]", String.class)
                .addStatement("$T value = request.getParam(key)", String.class)
                .beginControlFlow("if($T.isEmpty(value))", StringUtils.class)
                .addStatement("error = $S + key", "You must supply parameter: ")
                .addStatement("$T.processErrorResponse(error, request.response(), $T.BAD_REQUEST.code())",
                        HttpHelper.class, HttpResponseStatus.class)
                .addStatement("break")
                .nextControlFlow("else")
                .addStatement("queryKey[i] = value")
                .endControlFlow()
                .endControlFlow()
                .addStatement("return $T.isEmpty(error) ? $L : null", StringUtils.class, "queryKey")
                .build();
    }
}
