package com.cyngn.exovert.generate.server;

import com.cyngn.exovert.generate.server.config.ConfTemplate;
import com.cyngn.exovert.generate.server.config.LogbackTemplate;
import com.cyngn.exovert.util.Disk;
import com.cyngn.exovert.util.GeneratorHelper;
import com.cyngn.exovert.util.MetaData;
import com.cyngn.vertx.web.RestApi;
import com.cyngn.vertx.web.RouterTools;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.TableMetadata;
import com.englishtown.vertx.cassandra.impl.DefaultCassandraSession;
import com.englishtown.vertx.cassandra.impl.JsonCassandraConfigurator;
import com.google.common.base.CaseFormat;
import com.google.common.collect.Lists;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.LoggerHandler;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.List;

/**
 * Generates a simple CRUD Server.
 *
 * @author truelove@cyngn.com (Jeremy Truelove) 9/9/15
 */
public class ServerGenerator {
    private static String INITIALIZER_THREAD_FIELD = "INITIALIZER_THREAD_KEY";
    private static String SHARED_DATA_FIELD = "SHARED_DATA_KEY";

    /**
     * Kicks off simple server generation.
     * @param tables the cassandra table meta data
     * @throws IOException thrown if we fail to write out to disk
     */
    public static void generate(Collection<TableMetadata> tables) throws IOException {
        String namespaceToUse = MetaData.instance.getNamespace();

        TypeSpec.Builder serverBuilder = TypeSpec.classBuilder("Server")
                .addModifiers(Modifier.PUBLIC);

        serverBuilder.superclass(AbstractVerticle.class);

        addMemberVars(namespaceToUse, serverBuilder);

        serverBuilder.addMethod(getStartupMethod());
        serverBuilder.addMethod(getIsInitializerFunc());
        serverBuilder.addMethod(getBuildApi(tables));
        serverBuilder.addMethod(getStartServer());
        serverBuilder.addMethod(getStopMethod());

        serverBuilder.addJavadoc(GeneratorHelper.getJavaDocHeader("Simple server that registers all {@link " +
                ClassName.get(RestApi.class) + "} for CRUD operations." +
                "\n\nto build: ./gradlew clean shadowJar\n" +
                "to run: java -jar build/libs/[project-name]-fat.jar -conf [your_conf.json]", MetaData.instance.getUpdateTime()));

        JavaFile javaFile = JavaFile.builder(namespaceToUse, serverBuilder.build()).build();

        Disk.outputFile(javaFile);

        //setup the logback file the server needs to run
        Disk.outputFile(LogbackTemplate.TEMPLATE, "src/main/resources/logback.xml");
        // output a default conf file
        Disk.outputFile(ConfTemplate.TEMPLATE, "conf.json");
    }

    private static MethodSpec getStartupMethod() {
        return MethodSpec.methodBuilder("start")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterizedTypeName.get(Future.class, Void.class), "startedResult", Modifier.FINAL)
                .addStatement("$T config = config()", JsonObject.class)
                .addCode("\nif(!config.containsKey($S)) { stop(); }\n\n", "cassandra")
                .addStatement("$N = vertx.sharedData().getLocalMap($N)", "sharedData", SHARED_DATA_FIELD)
                .addStatement("$N.putIfAbsent($N, Thread.currentThread().getId())", "sharedData",
                        INITIALIZER_THREAD_FIELD)
                .addStatement("$N = new $T($T.builder(), new $T(vertx), vertx)", "session",
                        DefaultCassandraSession.class, Cluster.class, JsonCassandraConfigurator.class)
                .addStatement("$N = config.getInteger($S, 80)", "port", "port")
                .beginControlFlow("\nif(isInitializerThread())")
                .beginControlFlow("try")
                .addStatement("logger.info($S, $T.getLocalHost().getHostAddress(), $L)",
                        "Starting up server... on ip: {} port: {}", InetAddress.class, "port")
                .nextControlFlow("catch($T ex)", UnknownHostException.class)
                .addStatement("logger.error($S, ex)", "Failed to get host ip address, ex: ")
                .addStatement("stop()")
                .endControlFlow()
                .endControlFlow()
                .addCode("\n")
                .addStatement("startServer()")
                .addStatement("startedResult.complete()")
                .build();
    }

    private static MethodSpec getStopMethod() {
        return MethodSpec.methodBuilder("stop")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("logger.info($S)", "Stopping the server.")
                .beginControlFlow("try")
                .addCode("if(server != null) { server.close(); }\n")
                .nextControlFlow("finally")
                .addCode("//make sure only one thread tries to shutdown.\n")
                .addStatement("Long shutdownThreadId = sharedData.putIfAbsent($S, $T.currentThread().getId())",
                        "shutdown", Thread.class)
                .beginControlFlow("if(shutdownThreadId == null)")
                .addCode("vertx.close(event ->").beginControlFlow("")
                .addStatement("logger.info($S)", "Vertx shutdown")
                .addStatement("System.exit(-1)")
                .endControlFlow(")")
                .endControlFlow()
                .endControlFlow()
                .build();
    }

    private static MethodSpec getIsInitializerFunc() {
        return MethodSpec.methodBuilder("isInitializerThread")
                .returns(boolean.class)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return sharedData.get($N) == $T.currentThread().getId()", INITIALIZER_THREAD_FIELD,
                        Thread.class).build();
    }

    private static void addMemberVars(String namespace, TypeSpec.Builder builder) {
        builder.addField(GeneratorHelper.getLogger(namespace, "Server"));
        builder.addField(ParameterizedTypeName.get(LocalMap.class, String.class, Long.class), "sharedData",
                Modifier.PRIVATE);
        builder.addField(FieldSpec.builder(String.class, SHARED_DATA_FIELD, Modifier.PRIVATE, Modifier.STATIC,
                Modifier.FINAL)
                .initializer("$S", "shared_data").build());
        builder.addField(FieldSpec.builder(String.class,  INITIALIZER_THREAD_FIELD, Modifier.PRIVATE, Modifier.STATIC,
                Modifier.FINAL)
                .initializer("$S", "initializer_thread").build());
        builder.addField(HttpServer.class, "server", Modifier.PRIVATE);
        builder.addField(DefaultCassandraSession.class, "session", Modifier.PRIVATE);
        builder.addField(int.class, "port", Modifier.PRIVATE);
    }

    private static MethodSpec getStartServer() {
        return MethodSpec.methodBuilder("startServer")
                .addModifiers(Modifier.PRIVATE)
                .addStatement("$N = vertx.createHttpServer()", "server")
                .addStatement("$T router = $T.router(vertx)", Router.class, Router.class)
                .addStatement("buildApi(router)")
                .addStatement("server.requestHandler(router::accept)")
                .addCode("\nserver.listen(port, $S, event -> ", "0.0.0.0")
                .beginControlFlow("")
                .beginControlFlow("if(event.failed())")
                .addStatement("logger.error($S, event.cause())", "Failed to start server, error: ")
                .addStatement("stop()")
                .nextControlFlow("else")
                .addStatement("logger.info($S, Thread.currentThread().getId())", "Thread: {} starting to handle request")
                .endControlFlow()
                .endControlFlow(")")
                .build();
    }

    private static MethodSpec getBuildApi(Collection<TableMetadata> tables) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("buildApi")
                .addParameter(Router.class, "router")
                .addModifiers(Modifier.PRIVATE)
                .addStatement("$T.registerRootHandlers(router, $T.create())", RouterTools.class,
                        LoggerHandler.class)
                .addCode("\n")
                .addCode("$T<$T> apis = $T.newArrayList(", List.class, RestApi.class, Lists.class);

        CodeBlock.Builder block = CodeBlock.builder();
        block.indent();
        boolean first = true;
        for(TableMetadata table : tables) {
            String rootClass = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, table.getName());
            ClassName apiClass = ClassName.get(MetaData.instance.getRestNamespace(), rootClass + "Api");
            ClassName dalClass = ClassName.get(MetaData.instance.getDalNamespace(), rootClass + "Dal");

            if (first) {
                block.add("\nnew $T(new $T(session))", apiClass, dalClass);
                first = false;
            } else { block.add(",\nnew $T(new $T(session))", apiClass, dalClass); }
        }
        block.unindent();
        builder.addCode(block.build())
        .addCode("\n")
        .addStatement(")")
        .beginControlFlow("\nfor($T api: apis)", RestApi.class)
        .addStatement("api.init(router)")
        .addCode("if(isInitializerThread()) {api.outputApi(logger);}\n")
        .endControlFlow();

        return builder.build();
    }
}
