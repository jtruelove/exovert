package com.cyngn.exovert.generate.server.rest;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import jdk.nashorn.internal.ir.annotations.Immutable;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Implementation of {@link TypeMap}
 *
 * Coupled with {@link TypeName} to keep knowledge of types that are fed into
 * code generation.
 *
 * @author asarda@cyngn.com (Ajay Sarda) 9/8/15.
 */
class TypeMapImpl implements TypeMap {

    private Map<String, TypeName> typeToClassMapping = new HashMap<>();
    private Map<String, Function<CodeBlock, CodeBlock>> typeConverterMapping  = new HashMap<>();

    private void bootstrap() {
        typeToClassMapping.put("Object", TypeName.OBJECT);
        typeToClassMapping.put("Void", ClassName.get("java.lang", "Void"));
        typeToClassMapping.put("Boolean", ClassName.get("java.lang", "Boolean"));
        typeToClassMapping.put("Byte", ClassName.get("java.lang", "Byte"));
        typeToClassMapping.put("Short", ClassName.get("java.lang", "Short"));
        typeToClassMapping.put("Integer", ClassName.get("java.lang", "Integer"));
        typeToClassMapping.put("Long", ClassName.get("java.lang", "Long"));
        typeToClassMapping.put("Character", ClassName.get("java.lang", "Character"));
        typeToClassMapping.put("Float", ClassName.get("java.lang", "Float"));
        typeToClassMapping.put("Double", ClassName.get("java.lang", "Double"));

        typeToClassMapping.put("Date", ClassName.get("org.joda.time", "DateTime"));
        typeToClassMapping.put("String", ClassName.get("java.lang", "String"));
        typeToClassMapping.put("List", ClassName.get("java.util", "List"));
        typeToClassMapping.put("Map", ClassName.get("java.util", "Map"));
        typeToClassMapping.put("Set", ClassName.get("java.util", "Set"));

        //converters for type
        // for Boolean
        typeConverterMapping.put("Boolean", cb ->
                CodeBlock.builder().add("$T.parseBoolean(", typeToClassMapping.get("Boolean")).add(cb).add(")").build());

        // for Byte
        typeConverterMapping.put("Byte", cb ->
                CodeBlock.builder().add("$T.parseByte(", typeToClassMapping.get("Byte")).add(cb).add(")").build());

        // for Short
        typeConverterMapping.put("Short", cb ->
                CodeBlock.builder().add("$T.parseShort(", typeToClassMapping.get("Short")).add(cb).add(")").build());

        // for Integer
        typeConverterMapping.put("Integer", cb ->
                CodeBlock.builder().add("$T.parseInt(", typeToClassMapping.get("Integer")).add(cb).add(")").build());

        // for Float
        typeConverterMapping.put("Float", cb ->
                CodeBlock.builder().add("$T.parseFloat(", typeToClassMapping.get("Float")).add(cb).add(")").build());

        // for Double
        typeConverterMapping.put("Double", cb ->
                CodeBlock.builder().add("$T.parseDouble(", typeToClassMapping.get("Double")).add(cb).add(")").build());

        // for Long
        typeConverterMapping.put("Long", cb ->
                CodeBlock.builder().add("$T.parseLong(", typeToClassMapping.get("Long")).add(cb).add(")").build());

        // for Character
        typeConverterMapping.put("Character", cb ->
                CodeBlock.builder().add(cb+".charAt(0)").add(")").build());

        // for Date
        typeConverterMapping.put("Date", cb ->
                CodeBlock.builder().add("new $T(", typeToClassMapping.get("Date")).add(cb).add(")").build());

        // for String
        typeConverterMapping.put("String", cb ->
                CodeBlock.builder().add("$L", cb).build());

        //Conversion for map and list will be tricky, unless we define our own serialization and use it across the board.
    }

    public TypeMapImpl() {
        bootstrap();
    }

    @Override
    public TypeName getTypeName(String type) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(type), "type cannot be empty or null");

        TypeName typeName = typeToClassMapping.get(type);
        if (typeName == null) {
            throw new IllegalArgumentException("Invalid type: " + type);
        }
        return typeName;
    }

    @Override
    public void registerType(String type, TypeName typeName) {
        registerType(type, typeName, false);
    }

    @Override
    public void registerType(String type, TypeName typeName, boolean isEnum) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(type), "type cannot be empty or null");
        Preconditions.checkArgument(typeName != null, "TypeName cannot be empty or null");

        if (typeToClassMapping.containsKey(type)) {
            throw new IllegalArgumentException(String.format("Type:%s already registered", type));
        }

        typeToClassMapping.put(type, typeName);

        if (isEnum) {
            typeConverterMapping.put(type, cb ->
                    CodeBlock.builder().add("$T.fromValue(", typeToClassMapping.get(type)).add(cb).add(")").build());
        }
    }

    @Override
    public CodeBlock getTypeConverter(String type, CodeBlock cb) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(type), "type cannot be empty or null");
        Preconditions.checkArgument(cb != null, "cb == null");

        if (!typeConverterMapping.containsKey(type)) {
            throw new IllegalArgumentException("No converter found for type: " + type);
        }
        return typeConverterMapping.get(type).apply(cb);
    }
}


