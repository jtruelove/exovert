package com.cyngn.exovert.generate.entity;

import com.cyngn.exovert.util.MetaData;
import com.cyngn.exovert.util.Udt;
import com.datastax.driver.core.ColumnMetadata;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.TableMetadata;
import com.datastax.driver.core.UserType;
import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.Field;
import com.datastax.driver.mapping.annotations.Frozen;
import com.datastax.driver.mapping.annotations.FrozenKey;
import com.datastax.driver.mapping.annotations.FrozenValue;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.google.common.base.CaseFormat;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import org.apache.cassandra.db.marshal.SimpleDateType;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Shared functions for generating entity code.
 *
 * @author truelove@cyngn.com (Jeremy Truelove) 8/28/15
 */
public class EntityGeneratorHelper {

    /**
     * Handle getting the class names for parameterized types.
     *
     * @param type the cassandra data type to extract from
     * @return the parameterized type result
     */
    public static TypeResult getClassWithTypes(DataType type) {
        ClassName outer = ClassName.get(type.asJavaClass());

        List<TypeName> generics = new ArrayList<>();
        boolean hasFrozenType = false;
        for(DataType genericType : type.getTypeArguments()) {
            if(Udt.instance.isUdt(genericType)) {
                generics.add(MetaData.getClassNameForUdt((UserType) genericType));
                if(genericType.isFrozen()) {
                    hasFrozenType = true;
                }
            } else {
                generics.add(getType(genericType).box());
            }
        }
        return new TypeResult(ParameterizedTypeName.get(outer, generics.toArray(new TypeName[generics.size()])), hasFrozenType);
    }

    private static TypeName getType(DataType dType)
    {
        if (SimpleDateType.class.getTypeName().equals(dType.getCustomTypeClassName())) {
            return ClassName.get(Date.class);
        } else { return ClassName.get(dType.asJavaClass()); }
    }

    /**
     * Get a setter spec for a entity field.
     *
     * @param field the field name
     * @param type the cassandra field type
     * @return the setter method spec
     */
    public static MethodSpec getSetter(String field, DataType type) {
        String methodRoot = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, field);
        String paramName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, field);
        MethodSpec.Builder spec;

        if (type.getTypeArguments().size() == 0) {
            if(Udt.instance.isUdt(type)) {
                spec = MethodSpec.methodBuilder("set" + methodRoot).addParameter(MetaData.getClassNameForUdt((UserType) type), paramName);
            } else {
                spec = MethodSpec.methodBuilder("set" + methodRoot).addParameter(getType(type), paramName);
            }
        } else {
            TypeResult result = getClassWithTypes(type);
            spec = MethodSpec.methodBuilder("set" + methodRoot).addParameter(result.type, paramName);
        }
        spec.addModifiers(Modifier.PUBLIC).addStatement("this.$L = $L", paramName, paramName);

        return spec.build();
    }

    /**
     * Get a getter spec for a entity field.
     *
     * @param field the field name
     * @param type the cassandra field type
     * @return the getter method spec
     */
    public static MethodSpec getGetter(String field, DataType type) {
        String methodRoot = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, field);
        String paramName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, field);
        MethodSpec.Builder spec;

        if (type.getTypeArguments().size() == 0) {
            if(Udt.instance.isUdt(type)) {
                spec = MethodSpec.methodBuilder("get" + methodRoot).returns(MetaData.getClassNameForUdt((UserType) type));
            } else {
                spec = MethodSpec.methodBuilder("get" + methodRoot).returns(getType(type));
            }
        } else {
            TypeResult result = getClassWithTypes(type);
            spec = MethodSpec.methodBuilder("get" + methodRoot).returns(result.type);
        }
        spec.addModifiers(Modifier.PUBLIC).addStatement("return $L", paramName);

        return spec.build();
    }

    /**
     * Get a FieldSpec for an entity field.
     *
     * @param field the field name
     * @param type the field type
     * @param isUdtClass is this a UDT entity?
     * @return the FieldSpec representing the cassandra field
     */
    public static FieldSpec getFieldSpec(String field, DataType type, boolean isUdtClass) {
       return getFieldSpec(field, type, isUdtClass, new ArrayList<>());
    }

    /**
     * Get a FieldSpec for an entity field.
     *
     * @param field the field name
     * @param type the field type
     * @param isUdtClass is this a UDT entity?
     * @param extraAnnotations additional annotations to put on the field
     * @return the FieldSpec representing the cassandra field
     */
    public static FieldSpec getFieldSpec(String field, DataType type, boolean isUdtClass,
                                         List<AnnotationSpec> extraAnnotations) {
        String fieldName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, field);
        FieldSpec.Builder spec;

        boolean hasFrozen = type.isFrozen();
        if (type.getTypeArguments().size() == 0) {
            if(Udt.instance.isUdt(type)) {
                spec = FieldSpec.builder(MetaData.getClassNameForUdt((UserType)type), fieldName, Modifier.PUBLIC);
            } else {
                spec = FieldSpec.builder(getType(type), fieldName, Modifier.PUBLIC);
            }
        } else {
            TypeResult result = getClassWithTypes(type);
            hasFrozen |= result.hasFrozenType;
            spec = FieldSpec.builder(result.type, fieldName, Modifier.PUBLIC);
        }

        if(hasFrozen) { spec.addAnnotation(getFrozenValueAnnotation()); }
        if (isUdtClass) { spec.addAnnotation(getFieldAnnotation(field)); }
        else { spec.addAnnotation(getColumnAnnotation(field)); }
        spec.addAnnotation(MetaData.getJsonAnnotation(field));

        for(AnnotationSpec annotationSpec : extraAnnotations) {
            spec.addAnnotation(annotationSpec);
        }

        return spec.build();
    }

    /**
     * Get the Column annotation for a table field.
     * @param field the field name to put the annotation on
     * @return the annotation
     */
    public static AnnotationSpec getColumnAnnotation(String field) {
        AnnotationSpec.Builder builder = AnnotationSpec.builder(Column.class);
        if(MetaData.isSnakeCase(field)) {
            builder.addMember("value", "name = $S", field);
        }
        return builder.build();
    }

    /**
     * Get the Field annotation for a UDT field.
     * @param field the field name to put the annotation on
     * @return the annotation
     */
    public static AnnotationSpec getFieldAnnotation(String field) {
        AnnotationSpec.Builder builder = AnnotationSpec.builder(Field.class);
        if(MetaData.isSnakeCase(field)) {
            builder.addMember("value", "name = $S", field);
        }
        return builder.build();
    }

    /**
     * Get a FrozenKey annotation for a field.
     * @return the annotation
     */
    public static AnnotationSpec getFrozenKeyAnnotation() {
        return AnnotationSpec.builder(FrozenKey.class).build();
    }

    /**
     * Get a FrozenValue annotation for a field.
     * @return the annotation
     */
    public static AnnotationSpec getFrozenValueAnnotation() {
        return AnnotationSpec.builder(FrozenValue.class).build();
    }

    /**
     * Get the PartitionKey annotation for a Table field.
     * @param position the order of the field in the partition key
     * @return the annotation
     */
    public static AnnotationSpec getPartitionKeyAnnotation(int position) {
        return AnnotationSpec.builder(PartitionKey.class).addMember("value", "$L", position).build();
    }

    /**
     * Get the ClusteringColumn annotation for a Table field.
     * @param position the order of the field in the partition key
     * @return the annotation
     */
    public static AnnotationSpec getClusteringAnnotation(int position) {
        return AnnotationSpec.builder(ClusteringColumn.class).addMember("value", "$L", position).build();
    }

    /**
     * Given a list of class fields and the class name generate a 'toString' method.
     * @param fields the class fields
     * @param className the class name
     * @return a new toString method
     */
    public static MethodSpec getToString(List<String> fields, String className) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("toString")
                .addAnnotation(Override.class)
                .returns(String.class)
                .addModifiers(Modifier.PUBLIC);

        builder.addCode("return $S $L\n", className + "{", "+");
        boolean first = true;
        for (String field : fields) {
            String fieldStr = field + "=";
            if(!first) {
                fieldStr = ", " + fieldStr;
            } else {
                first = false;
            }

            builder.addCode("$S + $N +\n", fieldStr, field);
        }
        builder.addStatement("$S", "}");
        return builder.build();
    }

    /**
     * Get the primary key for a Cassandra table
     *
     * @param table the Cassandra table
     * @return the names in order of the primary key fields
     */
    public static List<String> getPrimaryKey(TableMetadata table) {
        return table.getPrimaryKey().stream().map(ColumnMetadata::getName).collect(Collectors.toList());
    }
}
