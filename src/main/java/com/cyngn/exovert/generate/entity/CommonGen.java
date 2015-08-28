package com.cyngn.exovert.generate.entity;

import com.cyngn.exovert.util.MetaData;
import com.cyngn.exovert.util.Udt;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.UserType;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.Field;
import com.datastax.driver.mapping.annotations.FrozenValue;
import com.google.common.base.CaseFormat;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * @author truelove@cyngn.com (Jeremy Truelove) 8/28/15
 */
public class CommonGen {
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
                generics.add(ClassName.get(genericType.asJavaClass()).box());
            }
        }
        return new TypeResult(ParameterizedTypeName.get(outer, generics.toArray(new TypeName[generics.size()])), hasFrozenType);
    }

    public static MethodSpec getSetter(String field, DataType type) {
        String methodRoot = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, field);
        String paramName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, field);
        MethodSpec.Builder spec;

        if (type.getTypeArguments().size() == 0) {
            if(Udt.instance.isUdt(type)) {
                spec = MethodSpec.methodBuilder("set" + methodRoot).addParameter(MetaData.getClassNameForUdt((UserType) type), paramName);
            } else {
                spec = MethodSpec.methodBuilder("set" + methodRoot).addParameter(type.asJavaClass(), paramName);
            }
        } else {
            TypeResult result = getClassWithTypes(type);
            spec = MethodSpec.methodBuilder("set" + methodRoot).addParameter(result.type, paramName);
        }
        spec.addModifiers(Modifier.PUBLIC).addStatement("this.$L = $L", paramName, paramName);

        return spec.build();
    }

    public static MethodSpec getGetter(String field, DataType type) {
        String methodRoot = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, field);
        String paramName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, field);
        MethodSpec.Builder spec;

        if (type.getTypeArguments().size() == 0) {
            if(Udt.instance.isUdt(type)) {
                spec = MethodSpec.methodBuilder("get" + methodRoot).returns(MetaData.getClassNameForUdt((UserType) type));
            } else {
                spec = MethodSpec.methodBuilder("get" + methodRoot).returns(type.asJavaClass());
            }
        } else {
            TypeResult result = getClassWithTypes(type);
            spec = MethodSpec.methodBuilder("get" + methodRoot).returns(result.type);
        }
        spec.addModifiers(Modifier.PUBLIC).addStatement("return $L", paramName);

        return spec.build();
    }

    public static FieldSpec getFieldSpec(String field, DataType type, boolean isUdtClass) {
        String fieldName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, field);
        FieldSpec.Builder spec;

        boolean hasFrozen = type.isFrozen();
        if (type.getTypeArguments().size() == 0) {
            if(Udt.instance.isUdt(type)) {
                spec = FieldSpec.builder(MetaData.getClassNameForUdt((UserType)type), fieldName, Modifier.PUBLIC);
            } else {
                spec = FieldSpec.builder(type.asJavaClass(), fieldName, Modifier.PUBLIC);
            }
        } else {
            TypeResult result = getClassWithTypes(type);
            hasFrozen |= result.hasFrozenType;
            spec = FieldSpec.builder(result.type, fieldName, Modifier.PUBLIC);
        }

        if(hasFrozen) { spec.addAnnotation(getFrozenAnnotation()); }
        if (isUdtClass) { spec.addAnnotation(getFieldAnnotation(field)); }
        else { spec.addAnnotation(getColumnAnnotation(field)); }
        spec.addAnnotation(MetaData.getJsonAnnotation(field));

        return spec.build();
    }

    public static AnnotationSpec getColumnAnnotation(String field) {
        AnnotationSpec.Builder builder = AnnotationSpec.builder(Column.class);
        if(MetaData.isSnakeCase(field)) {
            builder.addMember("value", "$S", field);
        }
        return builder.build();
    }


    public static AnnotationSpec getFieldAnnotation(String field) {
        AnnotationSpec.Builder builder = AnnotationSpec.builder(Field.class);
        if(MetaData.isSnakeCase(field)) {
            builder.addMember("value", "$S", field);
        }
        return builder.build();
    }

    public static AnnotationSpec getFrozenAnnotation() {
        return AnnotationSpec.builder(FrozenValue.class).build();
    }
}
