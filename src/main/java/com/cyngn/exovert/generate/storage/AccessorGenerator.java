package com.cyngn.exovert.generate.storage;

import com.cyngn.exovert.util.Disk;
import com.cyngn.exovert.util.GeneratorHelper;
import com.cyngn.exovert.util.MetaData;
import com.cyngn.exovert.util.Udt;
import com.datastax.driver.core.ColumnMetadata;
import com.datastax.driver.core.TableMetadata;
import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;
import com.google.common.base.CaseFormat;
import com.google.common.util.concurrent.ListenableFuture;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import org.apache.cassandra.db.marshal.SimpleDateType;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Handles generating a Cassandra accessor class.
 *
 * @author truelove@cyngn.com (Jeremy Truelove) 12/3/15
 */
public class AccessorGenerator {

    /**
     * Kicks off DAL generation.
     * @param tables the cassandra table meta data
     * @throws IOException if write to file fails
     */
    public static void generate(Collection<TableMetadata> tables) throws IOException {
        String namespaceToUse = MetaData.instance.getDalNamespace();

        for (TableMetadata table : tables) {
            String rawName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, table.getName());
            String name = rawName + "Accessor";

            ClassName entityTable = ClassName.get(MetaData.instance.getTableNamespace(), rawName);

            TypeSpec.Builder accessorBuilder = TypeSpec.interfaceBuilder(name)
                    .addModifiers(Modifier.PUBLIC);

            accessorBuilder.addAnnotation(Accessor.class);

            List<ColumnMetadata> pKeyColumns = table.getPrimaryKey();
            List<ColumnMetadata> partitionKeyColumns = table.getPartitionKey();

            int partitionKeys = partitionKeyColumns.size();
            // if there is only 1 key or there's just the partition key without clustering keys
            if (isSingleValueKeyedTable(table)) {
                accessorBuilder.addMethod(generateAll(table, entityTable));
            } else {
                // if there are clustering keys after the partition key we need to start at the partition key
                int count = partitionKeys > 1 ? partitionKeys : 1;
                while (count < pKeyColumns.size()) {
                    accessorBuilder.addMethod(generateSpecificGet(table, entityTable, count));
                    count++;
                }
            }

            accessorBuilder.addJavadoc(GeneratorHelper.getJavaDocHeader("Accessor for Cassandra entity - {@link " +
                    ClassName.get(MetaData.instance.getTableNamespace(), rawName) + "}", MetaData.instance.getUpdateTime()));

            JavaFile javaFile = JavaFile.builder(namespaceToUse, accessorBuilder.build()).build();

            Disk.outputFile(javaFile);
        }
    }

    public static boolean isSingleValueKeyedTable(TableMetadata table) {
        int primaryKeyCount = table.getPrimaryKey().size();
        int partitionKeyCount = table.getPartitionKey().size();
        return primaryKeyCount == 1 || primaryKeyCount == partitionKeyCount;
    }

    public static List<List<ParameterSpec>> getParametersForAccessors(TableMetadata table) {
        List<List<ParameterSpec>> methodParamPermutations = new ArrayList<>();
        List<ColumnMetadata> pKeys = table.getPrimaryKey();
        int partitionKeys = table.getPartitionKey().size();
        int primaryKeys = pKeys.size();

        // if there are clustering keys after the partition key we need to start at the partition key
        int count = partitionKeys > 1 ? partitionKeys : 1;
        while (count < primaryKeys) {
            methodParamPermutations.add(getParameters(table, count));
            count++;
        }

        return methodParamPermutations;
    }

    private static List<ParameterSpec> getParameters(TableMetadata table, int desiredColumns) {
        List<ColumnMetadata> columns = table.getPrimaryKey();
        List<ParameterSpec> fields = new ArrayList<>();
        for(int i = 0; i < desiredColumns; i++) {
            ColumnMetadata column = columns.get(i);
            fields.add(getSpec(column));
        }

        return fields;
    }

    private static MethodSpec generateAll(TableMetadata table, ClassName entityTable) {
        String query = getBaseQuery(table);

        return MethodSpec.methodBuilder("getAll")
                .addModifiers(Modifier.ABSTRACT).addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(ClassName.get(ListenableFuture.class),
                        ParameterizedTypeName.get(ClassName.get(Result.class), entityTable)))
                .addAnnotation(AnnotationSpec.builder(Query.class).addMember("value", "$S", query).build())
                .build();
    }

    private static MethodSpec generateSpecificGet(TableMetadata table, ClassName entityTable, int desiredColumns) {
        String query = getBaseQuery(table) + " WHERE ";
        MethodSpec.Builder builder = MethodSpec.methodBuilder("getAll");

        List<ColumnMetadata> columns = table.getPrimaryKey();
        for(int i = 0; i < desiredColumns; i++) {
            ColumnMetadata column = columns.get(i);
            String name = column.getName();
            String newClause = name + "=:" + name;
            if(i != 0) {
                newClause = " AND " + newClause;
            }
            query += newClause;
            builder.addParameter(getSpec(column, true));
        }

        return builder.addModifiers(Modifier.ABSTRACT).addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(ClassName.get(ListenableFuture.class),
                        ParameterizedTypeName.get(ClassName.get(Result.class), entityTable)))
                .addAnnotation(AnnotationSpec.builder(Query.class).addMember("value", "$S", query).build())
                .build();

    }

    private static ParameterSpec getSpec(ColumnMetadata column) {
        return getSpec(column, false);
    }

    private static ParameterSpec getSpec(ColumnMetadata column, boolean addAnnotation) {
        String name = column.getName();
        String paramName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name);
        ParameterSpec.Builder param;
        if (SimpleDateType.class.getTypeName().equals(column.getType().getCustomTypeClassName())) {
            param = ParameterSpec.builder(Date.class, paramName);
        } else if (Udt.instance.isUdt(column.getType())) {
            throw new IllegalArgumentException("We don't currently support UDT primary keys in the query string, field: "
                    + column.getName());
        } else {
            param = ParameterSpec.builder(column.getType().asJavaClass(), paramName);
        }

        if(addAnnotation) {
            param.addAnnotation(AnnotationSpec.builder(Param.class).addMember("value", "$S", name).build());
        }

        return param.addModifiers(Modifier.FINAL).build();
    }

    private static String getBaseQuery(TableMetadata table) {
        return String.format("SELECT * FROM %s.%s", table.getKeyspace().getName(), table.getName().toLowerCase());
    }
}
