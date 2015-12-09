package com.cyngn.exovert.generate.rest;

import com.cyngn.exovert.generate.entity.EntityGeneratorHelper;
import com.cyngn.exovert.generate.server.rest.TypeMap;
import com.cyngn.exovert.generate.storage.AccessorGenerator;
import com.cyngn.exovert.util.Disk;
import com.cyngn.exovert.util.GeneratorHelper;
import com.cyngn.exovert.util.MetaData;
import com.cyngn.exovert.util.Udt;
import com.cyngn.vertx.async.ResultContext;
import com.cyngn.vertx.web.HttpHelper;
import com.cyngn.vertx.web.JsonUtil;
import com.cyngn.vertx.web.RestApi;
import com.datastax.driver.core.ColumnMetadata;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.TableMetadata;
import com.google.common.base.CaseFormat;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.apache.cassandra.db.marshal.SimpleDateType;
import org.apache.commons.lang.StringUtils;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Generates a REST interface for doing CRUD operations
 *
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

            restBuilder.addJavadoc(GeneratorHelper.getJavaDocHeader("REST Api for Cassandra entity - {@link " +
                    ClassName.get(MetaData.instance.getTableNamespace(), rawName) + "}", MetaData.instance.getUpdateTime()));

            restBuilder.addSuperinterface(RestApi.class);
            addMemberVars(tableName, dalName, restBuilder);

            restBuilder.addMethod(getConstructor(dalName, EntityGeneratorHelper.getPrimaryKey(table)));

            addSave(restBuilder, rawName);
            restBuilder.addMethod(getDelete(rawName));
            restBuilder.addMethod(getGet(rawName));
            getGetAll(rawName, restBuilder, table);
            restBuilder.addMethod(getSupportedApi());
            restBuilder.addMethod(getQueryConverter(table));

            JavaFile javaFile = JavaFile.builder(namespaceToUse, restBuilder.build()).build();
            Disk.outputFile(javaFile);
        }
    }

    private static void createUtilClass(String namespaceToUse) throws IOException {
        TypeSpec.Builder utilBuilder = TypeSpec.classBuilder("RestUtil").addModifiers(Modifier.PUBLIC)
            .addJavadoc(GeneratorHelper.getJavaDocHeader
                    ("Central place to put shared functions for REST call processing.", MetaData.instance.getUpdateTime()))
            .addMethod(getIsValidMethod());
        utilBuilder.addMethod(generateGetAllProcessingMethod());
        utilBuilder.addField(GeneratorHelper.getLogger(namespaceToUse, "RestUtil"));
        JavaFile javaFile = JavaFile.builder(namespaceToUse, utilBuilder.build()).build();
        Disk.outputFile(javaFile);
    }

    private static MethodSpec getConstructor(String dalName, List<String> primaryKey) {
        MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get(MetaData.instance.getDalNamespace(), dalName), "storage", Modifier.FINAL)
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
        String apiConstantAll = rawClass.toUpperCase() + "_ALL_API";

        builder.addField(FieldSpec.builder(String.class, apiConstant, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", MetaData.instance.getRestPrefix() + rawClass).build());

        builder.addField(FieldSpec.builder(String.class, apiConstantAll, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", MetaData.instance.getRestPrefix() + rawClass + "_all").build());

        builder.addField(ClassName.get(storageNamespace, dalName), "storage", Modifier.PRIVATE, Modifier.FINAL);

        CodeBlock block = CodeBlock.builder().beginControlFlow("")
                .add("new RestApi.RestApiDescriptor($T.POST, $L, this::save),\n", HttpMethod.class, apiConstant)
                .add("new RestApi.RestApiDescriptor($T.GET, $L, this::get),\n", HttpMethod.class, apiConstant)
                .add("new RestApi.RestApiDescriptor($T.DELETE, $L, this::delete),\n", HttpMethod.class, apiConstant)
                .add("new RestApi.RestApiDescriptor($T.GET, $L, this::getAll)\n", HttpMethod.class, apiConstantAll)
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
                .addParameter(RoutingContext.class, "context", Modifier.FINAL)
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
                                "\nNOTE: this method is left intentionally package protected to allow you to call it in a different way\n",
                        clazz)
                .addParameter(RoutingContext.class, "context", Modifier.FINAL)
                .addParameter(Buffer.class, "body", Modifier.FINAL)
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
                .addStatement("String error = $S + entity.toString() + $S + result.error.getMessage()",
                        "Could not persist ", ", error: ")
                .addStatement("logger.error($S,  error)", "save - {}")
                .addStatement("$T.processErrorResponse(error, context.response(), $T.INTERNAL_SERVER_ERROR.code())",
                        HttpHelper.class, HttpResponseStatus.class)
                .nextControlFlow("else")
                .addStatement("String error = $S + entity.toString() + $S + result.errorMessage", "Could not persist ",
                        ", error: ")
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
                .addParameter(RoutingContext.class, "context", Modifier.FINAL)
                .addModifiers(Modifier.PUBLIC)
                .addCode("// if query params aren't valid a HttpResponseStatus.BAD_REQUEST will be sent with the missing field\n")
                .addStatement("$T queryKey = null", Object[].class)
                .beginControlFlow("if($T.isValid(context.request(), primaryKey))",
                        ClassName.get(MetaData.instance.getRestNamespace(), "RestUtil"))
                .addStatement("queryKey = convertQueryString($L)", "context")
                .endControlFlow()
                .beginControlFlow("\nif(queryKey != null)")
                .addCode("storage.delete(result -> ", Consumer.class)
                .beginControlFlow("")
                .beginControlFlow("if(result.succeeded)")
                .addStatement("$T.processResponse(context.response())", HttpHelper.class)
                .nextControlFlow("else if (result.error != null)")
                .addStatement("String error = $S + context.request().uri() + $S + result.error.getMessage()",
                        "Could not DELETE with query: ", " error: ")
                .addStatement("logger.error($S,  error)", "delete - {}")
                .addStatement("$T.processErrorResponse(error, context.response(), $T.INTERNAL_SERVER_ERROR.code())",
                        HttpHelper.class, HttpResponseStatus.class)
                .nextControlFlow("else")
                .addStatement("String error = $S + context.request().uri() + $S + result.error.getMessage()",
                        "Could not DELETE with query: ", " error: ")
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
                .addParameter(RoutingContext.class, "context", Modifier.FINAL)
                .addModifiers(Modifier.PUBLIC)
                .addCode("// if query params aren't valid a HttpResponseStatus.BAD_REQUEST will be sent with the missing field\n")
                .addStatement("$T queryKey = null", Object[].class)
                .beginControlFlow("if($T.isValid(context.request(), primaryKey))",
                        ClassName.get(MetaData.instance.getRestNamespace(), "RestUtil"))
                .addStatement("queryKey = convertQueryString($L)", "context")
                .endControlFlow()
                .beginControlFlow("\nif(queryKey != null)")
                .addCode("storage.get(result -> ", Consumer.class)
                .beginControlFlow("")
                .beginControlFlow("if(result.succeeded)")
                .beginControlFlow("if(result.value != null)")
                .addStatement("$T.processResponse(result.value, context.response())", HttpHelper.class)
                .nextControlFlow("else")
                .addStatement("$T.processResponse(context.response(), $T.NOT_FOUND.code())", HttpHelper.class,
                        HttpResponseStatus.class)
                .endControlFlow()
                .nextControlFlow("else if(result.error != null)")
                .addStatement("String error = $S + context.request().uri() + $S + result.error.getMessage()", "Could not GET with query: ", " error: ")
                .addStatement("logger.error($S,  error)", "get - {}")
                .addStatement("$T.processErrorResponse(error, context.response(), $T.INTERNAL_SERVER_ERROR.code())",
                        HttpHelper.class, HttpResponseStatus.class)
                .nextControlFlow("else")
                .addStatement("String error = $S + context.request().uri() + $S + result.error.getMessage()", "Could not GET with query: ", " error: ")
                .addStatement("logger.error($S, error)", "get - {}")
                .addStatement("$T.processErrorResponse(error, context.response(), $T.BAD_REQUEST.code())",
                        HttpHelper.class, HttpResponseStatus.class)
                .endControlFlow()
                .endControlFlow(", queryKey)")
                .endControlFlow()
                .build();
    }

    private static void getGetAll(String entityName, TypeSpec.Builder restBuilder, TableMetadata table) {
        ClassName clazz = ClassName.get(MetaData.instance.getTableNamespace(), entityName);

        MethodSpec.Builder builder = MethodSpec.methodBuilder("getAll")
                .addJavadoc(getJavaDoc("GetAll - gets a list of"), clazz)
                .addParameter(RoutingContext.class, "context", Modifier.FINAL)
                .addModifiers(Modifier.PUBLIC);

        if(AccessorGenerator.isSingleValueKeyedTable(table)) {
            builder.addCode("// there's only 1 key so we do a get all\n")
                .addStatement("storage.getAll(result -> $T.processGetAllResult(context, result))",
                        ClassName.get(MetaData.instance.getRestNamespace(), "RestUtil"));
        } else {
            List<List<ParameterSpec>> permutations = AccessorGenerator.getParametersForAccessors(table);

            builder.addStatement("HttpServerRequest request = context.request()")
                .addStatement("int paramCount = request.params().size()")
                .beginControlFlow("\ntry")
                .addCode("//the query must start at the partition key\n")
                .beginControlFlow("switch(paramCount)");
            TypeMap typeToConverters = TypeMap.create();
            for (List<ParameterSpec> params : permutations) {
                builder.addCode("//partial key: " + getKeyCommentString(params) + "\n")
                    .addCode("case $L:\n", params.size());

                builder.addCode("\tstorage.getAll(");
                for(ParameterSpec param : params) {
                    String columnName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, param.name);

                    ColumnMetadata column = table.getColumn(columnName);

                    if(column.getType().equals(DataType.text())) {
                        builder.addCode("request.getParam($S), ", columnName);
                    } else {
                        String type;
                        if (SimpleDateType.class.getTypeName().equals(column.getType().getCustomTypeClassName())) { type = "Date"; }
                        else { type = column.getType().asJavaClass().getSimpleName(); }

                        builder.addCode(typeToConverters.getTypeConverter(type,
                                CodeBlock.builder().add("request.getParam($S)", columnName).build())).addCode(", ");
                    }
                }
                builder.addStatement("result -> $T.processGetAllResult(context, result))",
                        ClassName.get(MetaData.instance.getRestNamespace(), "RestUtil"));
                builder.addStatement("\tbreak");
            }

            builder.addCode("default:\n")
                .addStatement("\tString error = $S + context.request().uri()", "Invalid get all query: ")
                .addStatement("\tlogger.error($S,  error)", "get_all - {}")
                .addStatement("\t$T.processErrorResponse(error, context.response(), $T.BAD_REQUEST.code())",
                        HttpHelper.class, HttpResponseStatus.class)
                .addStatement("\tbreak")
                .endControlFlow().nextControlFlow("catch (Exception ex)")
                .addStatement("\tString error = $S + context.request().uri() + $S + ex.getMessage()", "error with query: ", " error: ")
                .addStatement("\tlogger.error($S,  error, ex)", "get_all - {}")
                .addStatement("\t$T.processErrorResponse(error, context.response(), $T.INTERNAL_SERVER_ERROR.code())",
                        HttpHelper.class, HttpResponseStatus.class)
                .endControlFlow();
        }

        restBuilder.addMethod(builder.build());
    }

    private static String getKeyCommentString(List<ParameterSpec> params) {
        List<String> columns = params.stream().map(param -> param.name).collect(Collectors.toList());

        return StringUtils.join(columns, ", ");
    }

    private static MethodSpec generateGetAllProcessingMethod() {
        return MethodSpec.methodBuilder("processGetAllResult")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addTypeVariable(TypeVariableName.get("T"))
                .addJavadoc("Handles processing a get all result\n")
                .addParameter(RoutingContext.class, "context", Modifier.FINAL)
                .addParameter(ParameterizedTypeName.get(ClassName.get(ResultContext.class), ParameterizedTypeName.get(ClassName.get(List.class), TypeVariableName.get("T"))), "result", Modifier.FINAL)
                .beginControlFlow("if(result.succeeded)")
                .beginControlFlow("if(result.value != null)")
                .addStatement("$T.processResponse(result.value, context.response())", HttpHelper.class)
                .nextControlFlow("else")
                .addStatement("$T.processResponse(context.response(), $T.NOT_FOUND.code())", HttpHelper.class,
                        HttpResponseStatus.class)
                .endControlFlow()
                .nextControlFlow("else if(result.error != null)")
                .addStatement("String error = $S + context.request().uri() + $S + result.error.getMessage()", "Could not GET with query: ", " error: ")
                .addStatement("logger.error($S,  error)", "get_all - {}")
                .addStatement("$T.processErrorResponse(error, context.response(), $T.INTERNAL_SERVER_ERROR.code())",
                        HttpHelper.class, HttpResponseStatus.class)
                .nextControlFlow("else")
                .addStatement("String error = $S + context.request().uri() + $S + result.error.getMessage()", "Could not GET with query: ", " error: ")
                .addStatement("logger.error($S, error)", "get_all - {}")
                .addStatement("$T.processErrorResponse(error, context.response(), $T.BAD_REQUEST.code())",
                        HttpHelper.class, HttpResponseStatus.class)
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
        return MethodSpec.methodBuilder("isValid").addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(Boolean.class)
                .addJavadoc("Does the query string of this request contain the full primary key?\n")
                .addParameter(HttpServerRequest.class, "request", Modifier.FINAL)
                .addParameter(String[].class, PRIMARY_KEY_FIELD, Modifier.FINAL)
                .addStatement("$T error = null", String.class)
                .beginControlFlow("for(int i = 0; i < $L.length; i++)", PRIMARY_KEY_FIELD)
                .addStatement("$T key = primaryKey[i]", String.class)
                .addStatement("$T value = request.getParam(key)", String.class)
                .beginControlFlow("if($T.isEmpty(value))", StringUtils.class)
                .addStatement("error = $S + key", "You must supply parameter: ")
                .addStatement("$T.processErrorResponse(error, request.response(), $T.BAD_REQUEST.code())",
                        HttpHelper.class, HttpResponseStatus.class)
                .addStatement("break")
                .endControlFlow()
                .endControlFlow()
                .addStatement("return $T.isEmpty(error)", StringUtils.class)
                .build();
    }

    private static MethodSpec getQueryConverter(TableMetadata table) {

        List<ColumnMetadata> keys = table.getPrimaryKey();
        int numKeys = keys.size();
        TypeMap typeToConverters = TypeMap.create();

        MethodSpec.Builder builder = MethodSpec.methodBuilder("convertQueryString")
            .addJavadoc("Convert query params to their Cassandra type.\n")
            .addParameter(RoutingContext.class, "context", Modifier.FINAL)
            .addModifiers(Modifier.PUBLIC)
            .returns(Object[].class)
            .addStatement("$T request = context.request()", HttpServerRequest.class)
            .addCode("// if query params aren't valid a HttpResponseStatus.BAD_REQUEST will be sent with the missing field\n")
            .addStatement("$T values = new Object[$L]", Object[].class, keys.size());

        builder.beginControlFlow("try");
        for(int i = 0; i < numKeys; i++) {
            ColumnMetadata column = keys.get(i);
            if(column.getType().equals(DataType.text())) {
                builder.addStatement("values[$L] = request.getParam($S)", i, column.getName());
            } else if (Udt.instance.isUdt(column.getType())) {
                throw new IllegalArgumentException("We don't currently support UDT primary keys in the query string, field: "
                        + column.getName());
            } else {
                String type;
                if (SimpleDateType.class.getTypeName().equals(column.getType().getCustomTypeClassName())) { type = "Date"; }
                else { type = column.getType().asJavaClass().getSimpleName(); }

                builder.addCode("values[$L] = ", i)
                       .addCode(typeToConverters.getTypeConverter(type,
                                CodeBlock.builder().add("request.getParam($S)", column.getName()).build()))
                       .addStatement("");
            }
        }

        return builder.nextControlFlow("catch (Exception ex)")
               .addStatement("$T.processErrorResponse(ex.getMessage(), context.response(), $T.BAD_REQUEST.code())",
                       HttpHelper.class, HttpResponseStatus.class)
               .addStatement("return null")
               .endControlFlow()
               .addStatement("return values").build();
    }


}
