package com.cyngn.exovert.generate.storage;

import com.cyngn.exovert.util.Disk;
import com.cyngn.exovert.util.MetaData;
import com.datastax.driver.core.TableMetadata;
import com.englishtown.vertx.cassandra.CassandraSession;
import com.englishtown.vertx.cassandra.mapping.VertxMapper;
import com.englishtown.vertx.cassandra.mapping.VertxMappingManager;
import com.englishtown.vertx.cassandra.mapping.impl.DefaultVertxMappingManager;
import com.google.common.base.CaseFormat;
import com.google.common.util.concurrent.FutureCallback;
import com.squareup.javapoet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.Collection;
import java.util.function.BiConsumer;
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
     * @throws IOException
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

            dalBuilder.addField(getLogger(namespaceToUse, name));
            addCassandraObjects(entityTable, dalBuilder);
            dalBuilder.addMethod(getConstructor(entityTable));

            dalBuilder.addMethod(getSave(entityTable));
            dalBuilder.addMethod(getDelete(entityTable));
            dalBuilder.addMethod(getDeleteByKey(entityTable));
            dalBuilder.addMethod(getEntityGet(entityTable));

            dalBuilder.addJavadoc("GENERATED CODE DO NOT MODIFY, UNLESS YOU HATE YOURSELF\n"
                    + "\nDAL for Cassandra entity - " + rawName + "\n");

            dalBuilder.addSuperinterface(ParameterizedTypeName.get(ClassName.get(namespaceToUse, "CommonDal"), entityTable));

            JavaFile javaFile = JavaFile.builder(namespaceToUse, dalBuilder.build()).build();

            Disk.outputFile(javaFile);
        }
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
                        .addParameter(parameterizingType, "entity")
                        .addParameter(getOnComplete(), "onComplete")
                        .build())
                // get
                .addMethod(MethodSpec.methodBuilder("get")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .addParameter(getOnCompleteWithResult(parameterizingType), "onComplete")
                        .varargs(true)
                        .addParameter(Object[].class, "primaryKeys")
                        .build())
                // delete by key
                .addMethod(MethodSpec.methodBuilder("delete")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .addParameter(getOnComplete(), "onComplete")
                        .varargs(true)
                        .addParameter(Object[].class, "primaryKeys")
                        .build())
                // delete
                .addMethod(MethodSpec.methodBuilder("delete")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .addParameter(parameterizingType, "entity")
                        .addParameter(getOnComplete(), "onComplete")
                        .build())
                .addJavadoc("GENERATED CODE DO NOT MODIFY, UNLESS YOU HATE YOURSELF\n"
                        + "\ncommon interface for all DAL classes\n")
                .build();

        JavaFile javaFile = JavaFile.builder(namespace, commonDal).build();
        Disk.outputFile(javaFile);
    }

    private static FieldSpec getLogger(String nameSpace, String className) {
        return FieldSpec.builder(Logger.class, "logger", Modifier.FINAL, Modifier.STATIC)
                .initializer("$T.getLogger($T.class)", LoggerFactory.class, ClassName.get(nameSpace, className)).build();
    }

    private static void addCassandraObjects(ClassName tableEntity, TypeSpec.Builder builder) {
        builder.addField(FieldSpec.builder(CassandraSession.class, "session", Modifier.FINAL).build());

        TypeName [] types =  new TypeName [] {tableEntity};
        builder.addField(FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(VertxMapper.class),  types),
                "mapper", Modifier.FINAL).build());
    }

    private static MethodSpec getConstructor(ClassName tableEntity) {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(CassandraSession.class, "session")
                .addStatement("this.$N = $N", "session", "session")
                .addStatement("$T manager = new $T(session)", VertxMappingManager.class, DefaultVertxMappingManager.class)
                .addStatement("mapper = manager.mapper($T.class)", tableEntity)
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

        // setup the callback
        TypeSpec resultCallback = TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(ParameterizedTypeName.get(ClassName.get(FutureCallback.class), aVoid))
                .addMethod(MethodSpec.methodBuilder("onSuccess")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(aVoid, "result")
                        .addStatement("$L.accept(true)", "onComplete")
                        .build())
                .addMethod(MethodSpec.methodBuilder("onFailure")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(Throwable.class, "error")
                        .addStatement("logger.error(\"delete - {}, ex: \", $L, $L)", "primaryKey", "error")
                        .addStatement("$L.accept(false)", "onComplete")
                        .build())
                .build();

        return MethodSpec.methodBuilder("delete")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(getOnComplete(), "onComplete")
                .addParameter(Object[].class, "primaryKey")
                .varargs(true)
                .addStatement("logger.info(\"delete - {}\", $L)", "primaryKey")
                .addCode("\n")
                .addCode("mapper.deleteAsync($L, $L", resultCallback, "primaryKey")
                .addCode(");\n")
                .addJavadoc("Delete a $L object by key.\n", tableEntity.simpleName())
                .build();
    }

    private static MethodSpec getEntitySaveOrDelete(ClassName tableEntity, String method) {
        String entityParamName = getEntityParam(tableEntity);

        // setup the callback
        TypeSpec resultCallback = TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(ParameterizedTypeName.get(FutureCallback.class, Void.class))
                .addMethod(MethodSpec.methodBuilder("onSuccess")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(Void.class, "result")
                        .addStatement("$L.accept(true)", "onComplete")
                        .build())
                .addMethod(MethodSpec.methodBuilder("onFailure")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(Throwable.class, "error")
                        .addStatement("logger.error(\"$L - {}, ex: \", $L, $L)", method, entityParamName, "error")
                        .addStatement("$L.accept(false)", "onComplete")
                        .build())
                .build();


        return MethodSpec.methodBuilder(method)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(tableEntity, entityParamName)
                .addParameter(getOnComplete(), "onComplete")
                .addStatement("logger.info(\"$L - {}\", $L)", method, entityParamName)
                .addCode("\n")
                .addCode("mapper.$LAsync($L, $L", method, entityParamName, resultCallback)
                .addCode(");\n")
                .addJavadoc("$L a $L object.\n", CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, method), tableEntity.simpleName())
                .build();
    }

    private static MethodSpec getEntityGet(ClassName tableEntity) {
        // setup the callback
        TypeSpec resultCallback = TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(ParameterizedTypeName.get(ClassName.get(FutureCallback.class), tableEntity))
                .addMethod(MethodSpec.methodBuilder("onSuccess")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(tableEntity, "result")
                        .addStatement("$L.accept(true, result)", "onComplete")
                        .build())
                .addMethod(MethodSpec.methodBuilder("onFailure")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(Throwable.class, "error")
                        .addStatement("logger.error(\"get - {}, ex: \", $L, $L)", "primaryKey", "error")
                        .addStatement("$L.accept(false, null)", "onComplete")
                        .build())
                .build();

        return MethodSpec.methodBuilder("get")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(getOnCompleteWithResult(tableEntity), "onComplete")
                .addParameter(Object[].class, "primaryKey")
                .varargs(true)
                .addStatement("logger.info(\"get - {}\", $L)", "primaryKey")
                .addCode("\n")
                .addCode("mapper.getAsync($L, $L", resultCallback, "primaryKey")
                .addCode(");\n")
                .addJavadoc("Get a $L object by primary key.\n", tableEntity.simpleName())
                .build();
    }

    private static String getEntityParam(ClassName tableEntity) {
        // rename so the variable doesn't collide with any key words
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, tableEntity.simpleName()) + "Obj";
    }

    private static ParameterizedTypeName getOnComplete() {
        return ParameterizedTypeName.get(Consumer.class, Boolean.class);
    }

    private static ParameterizedTypeName getOnCompleteWithResult(TypeName type) {
        return ParameterizedTypeName.get(ClassName.get(BiConsumer.class), TypeName.get(Boolean.class), type);
    }
}
