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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        String namespaceToUse = MetaData.instance.getTableNamespace();

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
        Map<String, Integer> partitionKeys = getMapOfKeys(tableMetadata.getPartitionKey());
        Map<String, Integer> clusteringKeys = getMapOfKeys(tableMetadata.getClusteringColumns());

        for (ColumnMetadata column : tableMetadata.getColumns()) {
            DataType type = column.getType();
            String fieldName = column.getName();
            String name = column.getName();

            List<AnnotationSpec> extraAnnotations = new ArrayList<>();
            if(partitionKeys.containsKey(name)) {
                extraAnnotations.add(CommonGen.getPartitionKeyAnnotation(partitionKeys.get(name)));
            }

            if(clusteringKeys.containsKey(name)) {
                extraAnnotations.add(CommonGen.getClusteringAnnotation(clusteringKeys.get(name)));
            }

            builder.addField(CommonGen.getFieldSpec(fieldName, type, false, extraAnnotations));
            builder.addMethod(CommonGen.getSetter(fieldName, type));
            builder.addMethod(CommonGen.getGetter(fieldName, type));
        }
    }

    /**
     * Map out the keys and their positions.
     * @param keys the keys to map
     * @return map of the keys at their position in the key chain
     */
    private static Map<String,Integer> getMapOfKeys(List<ColumnMetadata> keys) {
        Map<String, Integer> partitionKeys = new HashMap<>();
        int count = 0;
        for(ColumnMetadata columnMetadata : keys) {
            partitionKeys.put(columnMetadata.getName(), count);
            count++;
        }
        return partitionKeys;
    }

    /**
     * Add Table annotation to class.
     */
    private static AnnotationSpec getTableAnnotation(String keyspace, String name) {
        return AnnotationSpec.builder(Table.class).addMember("keyspace", "$S", keyspace)
                .addMember("name", "$S", name).build();
    }
}
