package com.cyngn.exovert.generate.entity;

import com.cyngn.exovert.util.Disk;
import com.cyngn.exovert.util.MetaData;
import com.datastax.driver.core.ColumnMetadata;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.TableMetadata;
import com.datastax.driver.mapping.annotations.Table;
import com.google.common.base.CaseFormat;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.Collection;

/**
 * Creates Table classes based on the cassandra schema.
 *
 * @author truelove@cyngn.com (Jeremy Truelove) 8/27/15
 */
public class TableGenerator {

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
            String name = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, rawName);

            TypeSpec.Builder tableClassBuilder = TypeSpec.classBuilder(name)
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(getTableAnnotation(table.getKeyspace().getName(), rawName));

            addFields(tableClassBuilder, table);

            tableClassBuilder.addJavadoc("GENERATED CODE DO NOT MODIFY, UNLESS YOU HATE YOURSELF\n"
                    + "\nTable for Cassandra - " + rawName + "\n");


            JavaFile javaFile = JavaFile.builder(namespaceToUse, tableClassBuilder.build()).build();

            Disk.outputFile(javaFile);
        }
    }

    /**
     * Add fields to the class spec.
     */
    private static void addFields(TypeSpec.Builder builder, TableMetadata tableMetadata) {
        for (ColumnMetadata column : tableMetadata.getColumns()) {
            DataType type = column.getType();
            String fieldName = column.getName();

            builder.addField(CommonGen.getFieldSpec(fieldName, type, false));
            builder.addMethod(CommonGen.getSetter(fieldName, type));
            builder.addMethod(CommonGen.getGetter(fieldName, type));
        }
    }

    /**
     * Add Table annotation to class.
     */
    private static AnnotationSpec getTableAnnotation(String keyspace, String name) {
        return AnnotationSpec.builder(Table.class).addMember("keyspace", "$S", keyspace)
                .addMember("name", "$S", name).build();
    }
}
