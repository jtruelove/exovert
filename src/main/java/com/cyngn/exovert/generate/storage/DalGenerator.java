package com.cyngn.exovert.generate.storage;

import com.cyngn.exovert.util.Disk;
import com.cyngn.exovert.util.MetaData;
import com.datastax.driver.core.TableMetadata;
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
        String namespaceToUse = MetaData.instance.getTableNampspace();

        for (TableMetadata table : tables) {
            String rawName = table.getName();
            String name = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, rawName) + "Dal";

            TypeSpec.Builder dalBuilder = TypeSpec.classBuilder(name)
                    .addModifiers(Modifier.PUBLIC);

            FieldSpec.Builder fieldSpec = FieldSpec.builder(Logger.class, "logger", Modifier.FINAL).initializer("$T.getLogger($T)", LoggerFactory.class, ClassName.get(namespaceToUse, name));

            dalBuilder.addField(fieldSpec.build());

            dalBuilder.addJavadoc("GENERATED CODE DO NOT MODIFY, UNLESS YOU HATE YOURSELF\n"
                    + "\nTable for Cassandra - " + rawName + "\n");


            JavaFile javaFile = JavaFile.builder(namespaceToUse, dalBuilder.build()).build();

            Disk.outputFile(javaFile);
        }
    }

}
