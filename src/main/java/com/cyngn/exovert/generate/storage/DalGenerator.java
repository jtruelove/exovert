package com.cyngn.exovert.generate.storage;

import com.cyngn.exovert.util.Disk;
import com.cyngn.exovert.util.MetaData;
import com.datastax.driver.core.TableMetadata;
import com.englishtown.vertx.cassandra.CassandraSession;
import com.englishtown.vertx.cassandra.mapping.VertxMapper;
import com.englishtown.vertx.cassandra.mapping.VertxMappingManager;
import com.englishtown.vertx.cassandra.mapping.impl.DefaultVertxMappingManager;
import com.google.common.base.CaseFormat;
import com.squareup.javapoet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.Collection;

/**
 * @author truelove@cyngn.com (Jeremy Truelove) 8/28/15
 */
public class DalGenerator {


    /*

    private static final Logger logger = LoggerFactory.getLogger(ReportStorage.class);

    private final CassandraSession session;
    private final VertxMapper<MetricReport> mapper;

    public ReportStorage(CassandraSession session) {
        VertxMappingManager manager = new DefaultVertxMappingManager(session);
        mapper = manager.mapper(MetricReport.class);
        this.session = session;
    }
     */

    /**
     * Kicks off table generation.
     *
     * @param tables the cassandra table meta data
     * @throws IOException
     */
    public static void generate(Collection<TableMetadata> tables) throws IOException {
        String namespaceToUse = MetaData.instance.getDalNamespace();

        for (TableMetadata table : tables) {
            String rawName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, table.getName());
            String name = rawName + "Dal";

            ClassName entiyTable = ClassName.get(MetaData.instance.getTableNamespace(), rawName);

            TypeSpec.Builder dalBuilder = TypeSpec.classBuilder(name)
                    .addModifiers(Modifier.PUBLIC);

            dalBuilder.addField(getLogger(namespaceToUse, name));
            addCassandraObjects(entiyTable, dalBuilder);
            dalBuilder.addMethod(getConstructor(entiyTable));

            dalBuilder.addJavadoc("GENERATED CODE DO NOT MODIFY, UNLESS YOU HATE YOURSELF\n"
                    + "\nDAL for Cassandra entity - " + rawName + "\n");


            JavaFile javaFile = JavaFile.builder(namespaceToUse, dalBuilder.build()).build();

            Disk.outputFile(javaFile);
        }
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

}
