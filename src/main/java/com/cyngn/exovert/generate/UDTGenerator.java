package com.cyngn.exovert.generate;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.UserType;
import com.datastax.driver.mapping.annotations.Field;
import com.datastax.driver.mapping.annotations.UDT;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.CaseFormat;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Creates UDT classes based on
 *
 * @author truelove@cyngn.com (Jeremy Truelove) 8/27/15
 */
public class UDTGenerator {

    public static void generate(String outDir, String namespace, Collection<UserType> userTypes) throws IOException {
        String namespaceToUse = namespace + ".storage.udt";

        for (UserType userType : userTypes) {
            String rawName = userType.getTypeName();
            String name = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, rawName);

            TypeSpec.Builder udtClassBuilder = TypeSpec.classBuilder(name)
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(getUDTAnnotation(userType.getKeyspace(), rawName));

            addFields(udtClassBuilder, userType);

            udtClassBuilder.addJavadoc("GENERATED CODE DO NOT MODIFY, UNLESS YOU HATE YOURSELF\n"
                    + "\nUDT for Cassandra - " + rawName + "\n");


            JavaFile javaFile = JavaFile.builder(namespaceToUse, udtClassBuilder.build()).build();

            javaFile.writeTo(System.out);
        }
    }

    private static void addFields(TypeSpec.Builder builder, UserType userType) {
        for (String field : userType.getFieldNames()) {
            DataType type = userType.getFieldType(field);
            builder.addField(getFieldSpec(field, type));
            builder.addMethod(getSetter(field, type));
            builder.addMethod(getGetter(field, type));
        }
    }

    private static AnnotationSpec getUDTAnnotation(String keyspace, String udtName) {
        return AnnotationSpec.builder(UDT.class).addMember("keyspace", "$S", keyspace).addMember("name", "$S", udtName).build();
    }

    private static AnnotationSpec getFieldAnnotation(String field) {
        AnnotationSpec.Builder builder = AnnotationSpec.builder(Field.class);
        if(isSnakeCase(field)) {
            builder.addMember("value", "$S", field);
        }
        return builder.build();
    }

    public static boolean isSnakeCase(String str) {
        return str.contains("_");
    }

    private static AnnotationSpec getJsonAnnotation(String field) {
        AnnotationSpec.Builder builder = AnnotationSpec.builder(JsonProperty.class);
        if(isSnakeCase(field)) {
            builder.addMember("value", "$S", field);
        }

        return builder.build();
    }

    private static FieldSpec getFieldSpec(String field, DataType type) {
        String fieldName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, field);
        FieldSpec.Builder spec;

        if (type.getTypeArguments().size() == 0) {
            spec = FieldSpec.builder(type.asJavaClass(), fieldName, Modifier.PUBLIC);
        } else {
            spec = FieldSpec.builder(getClassWithTypes(type), fieldName, Modifier.PUBLIC);
        }

        spec.addAnnotation(getFieldAnnotation(field));
        spec.addAnnotation(getJsonAnnotation(field));

        return spec.build();
    }

    private static MethodSpec getSetter(String field, DataType type) {
        String methodRoot = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, field);
        String paramName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, field);
        MethodSpec.Builder spec;

        if (type.getTypeArguments().size() == 0) {
            spec = MethodSpec.methodBuilder("set" + methodRoot).addParameter(type.asJavaClass(), paramName);
        } else {
            spec = MethodSpec.methodBuilder("set" + methodRoot).addParameter(getClassWithTypes(type), paramName);
        }
        spec.addModifiers(Modifier.PUBLIC).addStatement("this.$L = $L", paramName, paramName);

        return spec.build();
    }

    private static MethodSpec getGetter(String field, DataType type) {
        String methodRoot = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, field);
        String paramName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, field);
        MethodSpec.Builder spec;

        if (type.getTypeArguments().size() == 0) {
            spec = MethodSpec.methodBuilder("get" + methodRoot).returns(type.asJavaClass());
        } else {
            spec = MethodSpec.methodBuilder("get" + methodRoot).returns(getClassWithTypes(type));
        }
        spec.addModifiers(Modifier.PUBLIC).addStatement("return $L", paramName);

        return spec.build();
    }

    private static TypeName getClassWithTypes(DataType type) {
        ClassName outer = ClassName.get(type.asJavaClass());

        List<TypeName> generics = new ArrayList<>();
        for(DataType genericType : type.getTypeArguments()) {
            generics.add(ClassName.get(genericType.asJavaClass()).box());
        }

        return ParameterizedTypeName.get(outer, generics.toArray(new TypeName[generics.size()]));
    }
}
