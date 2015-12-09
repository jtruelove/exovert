package com.cyngn.exovert.generate.storage;

import com.cyngn.exovert.util.Disk;
import com.cyngn.exovert.util.GeneratorHelper;
import com.cyngn.exovert.util.MetaData;
import com.cyngn.vertx.async.ResultContext;
import com.datastax.driver.core.TableMetadata;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.Result;
import com.englishtown.vertx.cassandra.CassandraSession;
import com.englishtown.vertx.cassandra.FutureUtils;
import com.englishtown.vertx.cassandra.mapping.VertxMapper;
import com.englishtown.vertx.cassandra.mapping.VertxMappingManager;
import com.englishtown.vertx.cassandra.mapping.impl.DefaultVertxMappingManager;
import com.google.common.base.CaseFormat;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import io.vertx.core.Vertx;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * Handles generating a DAL for each Cassandra Table entity.
 *
 * @author truelove@cyngn.com (Jeremy Truelove) 8/28/15
 */
public class DalGenerator {
    /**
     * Kicks off DAL generation.
     * @param tables the cassandra table meta data
     * @throws IOException if write to file fails
     */
    public static void generate(Collection<TableMetadata> tables) throws IOException {
        String namespaceToUse = MetaData.instance.getDalNamespace();
        generateDalInterface(namespaceToUse);

        for (TableMetadata table : tables) {
            String rawName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, table.getName());
            String name = rawName + "Dal";

            ClassName entityTable = ClassName.get(MetaData.instance.getTableNamespace(), rawName);

            TypeSpec.Builder dalBuilder = TypeSpec.classBuilder(name)
                    .addModifiers(Modifier.PUBLIC);

            dalBuilder.addField(GeneratorHelper.getLogger(namespaceToUse, name));
            addCassandraObjects(entityTable, dalBuilder);
            dalBuilder.addMethod(getConstructor(entityTable));
            dalBuilder.addField(Vertx.class, "vertx", Modifier.FINAL);

            dalBuilder.addMethod(getSave(entityTable));
            dalBuilder.addMethod(getDelete(entityTable));
            dalBuilder.addMethod(getDeleteByKey(entityTable));
            dalBuilder.addMethod(getEntityGet(entityTable));
            
            addGetAllMethods(table, entityTable, dalBuilder);

            dalBuilder.addJavadoc(GeneratorHelper.getJavaDocHeader("DAL for Cassandra entity - {@link " +
                    ClassName.get(MetaData.instance.getTableNamespace(), rawName) + "}", MetaData.instance.getUpdateTime()));

            dalBuilder.addSuperinterface(ParameterizedTypeName.get(ClassName.get(namespaceToUse, "CommonDal"), entityTable));

            JavaFile javaFile = JavaFile.builder(namespaceToUse, dalBuilder.build()).build();

            Disk.outputFile(javaFile);
        }
    }

    private static void addGetAllMethods(TableMetadata table, ClassName entityTable, TypeSpec.Builder builder) {
        ParameterizedTypeName callback = getOnCompleteWithListResult(entityTable);

        // get all use case
        if (AccessorGenerator.isSingleValueKeyedTable(table)) {
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("getAll")
                    .addModifiers(Modifier.PUBLIC);
            methodBuilder.addParameter(callback, "onComplete", Modifier.FINAL);

            addParamLogging("info", methodBuilder, new ArrayList<>());

            methodBuilder.addStatement("$T future = accessor.getAll()", ParameterizedTypeName.get(ListenableFuture.class),
                    ParameterizedTypeName.get(ClassName.get(Result.class), entityTable));

            methodBuilder.addStatement("$T.addCallback(future, $L, vertx)", FutureUtils.class,
                    getGetAllResultCallback(new ArrayList<>(), entityTable));
            builder.addMethod(methodBuilder.build());
        } else {
            List<List<ParameterSpec>> permutations = AccessorGenerator.getParametersForAccessors(table);
            for (List<ParameterSpec> params : permutations) {
                MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("getAll")
                        .addModifiers(Modifier.PUBLIC)
                        .addJavadoc("Get all matching {@link $L}(s) by sub key.\n", entityTable);
                addParamLogging("info", methodBuilder, params);

                // add method parameters
                params.forEach(methodBuilder::addParameter);
                methodBuilder.addParameter(callback, "onComplete", Modifier.FINAL);

                methodBuilder.addCode("\n$T future = accessor.getAll(", ParameterizedTypeName.get(ClassName.get(ListenableFuture.class),
                        ParameterizedTypeName.get(ClassName.get(Result.class), entityTable)));
                boolean first = true;
                for (ParameterSpec param : params) {
                    if(first) {
                        methodBuilder.addCode("$L", param.name);
                        first = false;
                    } else { methodBuilder.addCode(", $L", param.name); }
                }
                methodBuilder.addStatement(")");

                methodBuilder.addStatement("$T.addCallback(future, $L, vertx)", FutureUtils.class,
                        getGetAllResultCallback(params, entityTable));
                builder.addMethod(methodBuilder.build());
            }
        }
    }

    private static void addParamLogging(String logLevel, MethodSpec.Builder builder, List<ParameterSpec> params) {
        String logLevelLocal = logLevel.toLowerCase();
        builder.addCode("logger." + logLevelLocal + "(\"getAll -");
        for(ParameterSpec param : params) { builder.addCode(" " + param.name + ": {}"); }

        if("error".equals(logLevelLocal)) { builder.addCode(" ex: \""); }
        else { builder.addCode("\""); }

        for(ParameterSpec param : params) { builder.addCode(", $L", param.name); }

        if("error".equals(logLevelLocal)) { builder.addStatement(", $L)", "error"); }
        else { builder.addStatement(")"); }
    }

    private static TypeSpec getGetAllResultCallback(List<ParameterSpec> params, ClassName entityTable) {
        MethodSpec.Builder failureHandler = MethodSpec.methodBuilder("onFailure");

        addParamLogging("error", failureHandler, params);
        failureHandler.addStatement("$L.accept(new $T(error, $S))", "onComplete", ResultContext.class, "Failed to get all.");

        // setup the callback
        TypeSpec.Builder resultCallback = TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(ParameterizedTypeName.get(ClassName.get(FutureCallback.class),
                        ParameterizedTypeName.get(ClassName.get(Result.class), entityTable)))
                .addMethod(MethodSpec.methodBuilder("onSuccess")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ParameterizedTypeName.get(ClassName.get(Result.class), entityTable), "result")
                        .addStatement("$L.accept(new ResultContext(true, result.all()))", "onComplete")
                        .build());

        return resultCallback.addMethod(failureHandler
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Throwable.class, "error")
                .build())
                .build();
    }

    /**
     * Create a general interface for all DALs to confirm to
     * @param namespace the namespace to create the interface in
     * @throws IOException
     */
    private static void generateDalInterface(String namespace) throws IOException {
        TypeVariableName parameterizingType = TypeVariableName.get("T");

        TypeSpec commonDal = TypeSpec.interfaceBuilder("CommonDal")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(parameterizingType)
                // save
                .addMethod(MethodSpec.methodBuilder("save")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .addParameter(parameterizingType, "entity", Modifier.FINAL)
                        .addParameter(getOnComplete(), "onComplete", Modifier.FINAL)
                        .build())
                // get
                .addMethod(MethodSpec.methodBuilder("get")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .addParameter(getOnCompleteWithResult(parameterizingType), "onComplete", Modifier.FINAL)
                        .varargs(true)
                        .addParameter(Object[].class, "primaryKeys", Modifier.FINAL)
                        .build())
                // delete by key
                .addMethod(MethodSpec.methodBuilder("delete")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .addParameter(getOnComplete(), "onComplete", Modifier.FINAL)
                        .varargs(true)
                        .addParameter(Object[].class, "primaryKeys", Modifier.FINAL)
                        .build())
                // delete
                .addMethod(MethodSpec.methodBuilder("delete")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .addParameter(parameterizingType, "entity", Modifier.FINAL)
                        .addParameter(getOnComplete(), "onComplete", Modifier.FINAL)
                        .build())
                .addJavadoc(GeneratorHelper.getJavaDocHeader("Common interface for all DAL classes", MetaData.instance.getUpdateTime()))
                .build();

        JavaFile javaFile = JavaFile.builder(namespace, commonDal).build();
        Disk.outputFile(javaFile);
    }

    private static void addCassandraObjects(ClassName tableEntity, TypeSpec.Builder builder) {
        builder.addField(FieldSpec.builder(CassandraSession.class, "session", Modifier.FINAL).build());

        TypeName [] types =  new TypeName [] {tableEntity};
        builder.addField(FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(VertxMapper.class),  types),
                "mapper", Modifier.FINAL).build());

        builder.addField(ClassName.get(MetaData.instance.getDalNamespace(), tableEntity.simpleName() + "Accessor"),
                "accessor", Modifier.FINAL);
    }

    private static MethodSpec getConstructor(ClassName tableEntity) {

        ClassName accessorClass = ClassName.get(MetaData.instance.getDalNamespace(), tableEntity.simpleName() + "Accessor");

        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(CassandraSession.class, "session", Modifier.FINAL)
                .addStatement("this.$N = $N", "session", "session")
                .addStatement("$T manager = new $T(session)", VertxMappingManager.class, DefaultVertxMappingManager.class)
                .addStatement("mapper = manager.mapper($T.class)", tableEntity)
                .addStatement("$T accessorMappingManager = new $T(session.getSession())", MappingManager.class, MappingManager.class)
                .addStatement("accessor = accessorMappingManager.createAccessor($T.class)", accessorClass)
                .addStatement("vertx = session.getVertx()")
                .build();
    }

    private static MethodSpec getSave(ClassName tableEntity) {
       return getEntitySaveOrDelete(tableEntity, "save");
    }

    private static MethodSpec getDelete(ClassName tableEntity) {
        return getEntitySaveOrDelete(tableEntity, "delete");
    }

    private static MethodSpec getDeleteByKey(ClassName tableEntity) {
        TypeName aVoid = ClassName.get(Void.class);
        String simpleName = tableEntity.simpleName();

        // setup the callback
        TypeSpec resultCallback = TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(ParameterizedTypeName.get(ClassName.get(FutureCallback.class), aVoid))
                .addMethod(MethodSpec.methodBuilder("onSuccess")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(aVoid, "result")
                        .addStatement("$L.accept(new ResultContext(true))", "onComplete")
                        .build())
                .addMethod(MethodSpec.methodBuilder("onFailure")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(Throwable.class, "error")
                        .addStatement("logger.error(\"delete - {}, ex: \", $L, $L)", "primaryKey", "error")
                        .addStatement("$L.accept(new ResultContext(error, $L))", "onComplete",
                                "\"Failed to delete " + simpleName + " by key: \" +  primaryKey")
                        .build())
                .build();

        return MethodSpec.methodBuilder("delete")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(getOnComplete(), "onComplete", Modifier.FINAL)
                .addParameter(Object[].class, "primaryKey", Modifier.FINAL)
                .varargs(true)
                .addStatement("logger.info(\"delete - {}\", $L)", "primaryKey")
                .addCode("\n")
                .addCode("mapper.deleteAsync($L, $L", resultCallback, "primaryKey")
                .addCode(");\n")
                .addJavadoc("Delete a {@link $L} object by key.\n", tableEntity)
                .build();
    }

    private static MethodSpec getEntitySaveOrDelete(ClassName tableEntity, String method) {
        String entityParamName = getEntityParam(tableEntity);
        String simpleName = tableEntity.simpleName();

        // setup the callback
        TypeSpec resultCallback = TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(ParameterizedTypeName.get(FutureCallback.class, Void.class))
                .addMethod(MethodSpec.methodBuilder("onSuccess")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(Void.class, "result")
                        .addStatement("$L.accept(new ResultContext(true))", "onComplete")
                        .build())
                .addMethod(MethodSpec.methodBuilder("onFailure")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(Throwable.class, "error")
                        .addStatement("logger.error(\"$L - {}, ex: \", $L, $L)", method, entityParamName, "error")
                        .addStatement("$L.accept(new ResultContext(error, $L))", "onComplete",
                                "\"Failed to " + method + " " + simpleName + ": \" +  " + entityParamName)
                        .build())
                .build();


        return MethodSpec.methodBuilder(method)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(tableEntity, entityParamName, Modifier.FINAL)
                .addParameter(getOnComplete(), "onComplete", Modifier.FINAL)
                .addStatement("logger.info(\"$L - {}\", $L)", method, entityParamName)
                .addCode("\n")
                .addCode("mapper.$LAsync($L, $L", method, entityParamName, resultCallback)
                .addCode(");\n")
                .addJavadoc("$L a {@link $L} object.\n", CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, method),
                        tableEntity)
                .build();
    }

    private static MethodSpec getEntityGet(ClassName tableEntity) {
        String simpleName = tableEntity.simpleName();

        // setup the callback
        TypeSpec resultCallback = TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(ParameterizedTypeName.get(ClassName.get(FutureCallback.class), tableEntity))
                .addMethod(MethodSpec.methodBuilder("onSuccess")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(tableEntity, "result")
                        .addStatement("$L.accept(new ResultContext(true, result))", "onComplete")
                        .build())
                .addMethod(MethodSpec.methodBuilder("onFailure")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(Throwable.class, "error")
                        .addStatement("logger.error(\"get - {}, ex: \", $L, $L)", "primaryKey", "error")
                        .addStatement("$L.accept(new ResultContext(error, $L))", "onComplete",
                                "\"Failed to get " + simpleName + " by key: \" +  primaryKey")
                        .build())
                .build();

        return MethodSpec.methodBuilder("get")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(getOnCompleteWithResult(tableEntity), "onComplete", Modifier.FINAL)
                .addParameter(Object[].class, "primaryKey", Modifier.FINAL)
                .varargs(true)
                .addStatement("logger.info(\"get - {}\", $L)", "primaryKey")
                .addCode("\n")
                .addCode("mapper.getAsync($L, $L", resultCallback, "primaryKey")
                .addCode(");\n")
                .addJavadoc("Get a {@link $L} object by primary key.\n", tableEntity)
                .build();
    }

    private static String getEntityParam(ClassName tableEntity) {
        // rename so the variable doesn't collide with any key words
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, tableEntity.simpleName()) + "Obj";
    }

    private static ParameterizedTypeName getOnComplete() {
        return ParameterizedTypeName.get(Consumer.class, ResultContext.class);
    }

    private static ParameterizedTypeName getOnCompleteWithResult(TypeName type) {
        return ParameterizedTypeName.get(ClassName.get(Consumer.class),
                ParameterizedTypeName.get(ClassName.get(ResultContext.class), type));
    }

    private static ParameterizedTypeName getOnCompleteWithListResult(TypeName type) {
        return ParameterizedTypeName.get(ClassName.get(Consumer.class),
                ParameterizedTypeName.get(ClassName.get(ResultContext.class),
                        ParameterizedTypeName.get(ClassName.get(List.class), type)));
    }
}
