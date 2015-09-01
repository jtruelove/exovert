package com.cyngn.exovert.generate.entity;

import com.cyngn.exovert.util.Disk;
import com.cyngn.exovert.util.MetaData;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.UserType;
import com.datastax.driver.mapping.annotations.UDT;
import com.google.common.base.CaseFormat;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Creates Udt classes based on the cassandra schema.
 *
 * @author truelove@cyngn.com (Jeremy Truelove) 8/27/15
 */
public class UDTGenerator {

    /**
     * Kicks off table generation.
     *
     * @param userTypes the cassandra Udt meta data
     * @throws IOException
     */
    public static void generate(Collection<UserType> userTypes) throws IOException {
        String namespaceToUse = MetaData.instance.getUdtNamespace();

        for (UserType userType : userTypes) {
            String rawName = userType.getTypeName();
            String name = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, rawName);

            TypeSpec.Builder udtClassBuilder = TypeSpec.classBuilder(name)
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(getUDTAnnotation(userType.getKeyspace(), rawName));

            addFields(udtClassBuilder, userType, name);

            udtClassBuilder.addJavadoc(MetaData.getJavaDocHeader("UDT for Cassandra - " + rawName));

            JavaFile javaFile = JavaFile.builder(namespaceToUse, udtClassBuilder.build()).build();

            Disk.outputFile(javaFile);
        }
    }

    /**
     * Add fields to the class spec.
     */
    private static void addFields(TypeSpec.Builder builder, UserType userType, String className) {

        List<String> fields = new ArrayList<>();
        for (String field : userType.getFieldNames()) {
            DataType type = userType.getFieldType(field);
            builder.addField(EntityGeneratorHelper.getFieldSpec(field, type, true));
            builder.addMethod(EntityGeneratorHelper.getSetter(field, type));
            builder.addMethod(EntityGeneratorHelper.getGetter(field, type));

            fields.add(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, field));
        }

        builder.addMethod(EntityGeneratorHelper.getToString(fields, className));
    }

    /**
     * Add UDT annotation to class.
     */
    private static AnnotationSpec getUDTAnnotation(String keyspace, String udtName) {
        return AnnotationSpec.builder(UDT.class).addMember("keyspace", "$S", keyspace).addMember("name", "$S", udtName).build();
    }
}
