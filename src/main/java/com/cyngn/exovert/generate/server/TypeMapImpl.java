package com.cyngn.exovert.generate.server;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Implementation of {@link TypeMap}
 *
 * Coupled with {@link TypeName} to keep knowledge of types that are fed into
 * code generation.
 *
 * @author asarda@cyngn.com (Ajay Sarda) 9/8/15.
 */
public class TypeMapImpl implements TypeMap {

    private Map<String, TypeName> typeToClassMapping = new HashMap<>();
    private Map<String, Function<CodeBlock, CodeBlock>> typeConverterMapping  = new HashMap<>();

    private void bootstrap() {

        // bootstrap with all primitives and their boxed variants
        typeToClassMapping.put("void", TypeName.VOID);
        typeToClassMapping.put("boolean", TypeName.BOOLEAN);
        typeToClassMapping.put("byte", TypeName.BYTE);
        typeToClassMapping.put("short", TypeName.SHORT);
        typeToClassMapping.put("int", TypeName.INT);
        typeToClassMapping.put("long", TypeName.LONG);
        typeToClassMapping.put("char", TypeName.CHAR);
        typeToClassMapping.put("float", TypeName.FLOAT);
        typeToClassMapping.put("double", TypeName.DOUBLE);

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

        //converters for type
        // for boolean
        typeConverterMapping.put("boolean", cb ->
                CodeBlock.builder().add("$T.parseBoolean(", typeToClassMapping.get("Boolean")).add(cb).addStatement(")").build());
        typeConverterMapping.put("Boolean", cb ->
                CodeBlock.builder().add("$T.parseBoolean(", typeToClassMapping.get("Boolean")).add(cb).addStatement(")").build());

        // for byte
        typeConverterMapping.put("byte", cb ->
                CodeBlock.builder().add("$T.parseByte(", typeToClassMapping.get("Byte")).add(cb).addStatement(")").build());
        typeConverterMapping.put("Byte", cb ->
                CodeBlock.builder().add("$T.parseByte(", typeToClassMapping.get("Byte")).add(cb).addStatement(")").build());

        // for short
        typeConverterMapping.put("short", cb ->
                CodeBlock.builder().add("$T.parseShort(", typeToClassMapping.get("Short")).add(cb).addStatement(")").build());
        typeConverterMapping.put("Short", cb ->
                CodeBlock.builder().add("$T.parseShort(", typeToClassMapping.get("Short")).add(cb).addStatement(")").build());

        // for integer
        typeConverterMapping.put("int", cb ->
                CodeBlock.builder().add("$T.parseInt(", typeToClassMapping.get("Integer")).add(cb).addStatement(")").build());
        typeConverterMapping.put("Integer", cb ->
                CodeBlock.builder().add("$T.parseInt(", typeToClassMapping.get("Integer")).add(cb).addStatement(")").build());

        // for float
        typeConverterMapping.put("float", cb ->
                CodeBlock.builder().add("$T.parseFloat(", typeToClassMapping.get("Float")).add(cb).addStatement(")").build());
        typeConverterMapping.put("Float", cb ->
                CodeBlock.builder().add("$T.parseFloat(", typeToClassMapping.get("Float")).add(cb).addStatement(")").build());

        // for double
        typeConverterMapping.put("double", cb ->
                CodeBlock.builder().add("$T.parseDouble(", typeToClassMapping.get("Double")).add(cb).addStatement(")").build());
        typeConverterMapping.put("Double", cb ->
                CodeBlock.builder().add("$T.parseDouble(", typeToClassMapping.get("Double")).add(cb).addStatement(")").build());

        // for long
        typeConverterMapping.put("long", cb ->
                CodeBlock.builder().add("$T.parseLong(", typeToClassMapping.get("Long")).add(cb).addStatement(")").build());
        typeConverterMapping.put("Long", cb ->
                CodeBlock.builder().add("$T.parseLong(", typeToClassMapping.get("Long")).add(cb).addStatement(")").build());

        // for char
        typeConverterMapping.put("char", cb ->
                CodeBlock.builder().add(cb+".charAt(0)").addStatement(")").build());
        typeConverterMapping.put("Character", cb ->
                CodeBlock.builder().add(cb+".charAt(0)").addStatement(")").build());

        // for Date
        typeConverterMapping.put("Date", cb ->
                CodeBlock.builder().add("new $T(", typeToClassMapping.get("Date")).add(cb).addStatement(")").build());

        // for String
        typeConverterMapping.put("String", str ->
                CodeBlock.builder().addStatement("$L", str).build());

        //TODO: add list, map types.
        //Conversion will be tricky, unless we define our own serialization and use it across the board.
    }

    public TypeMapImpl() {
        bootstrap();
    }

    @Override
    public TypeName getTypeName(String type) {
        TypeName typeName = typeToClassMapping.get(type);
        if (typeName == null) {
            throw new IllegalArgumentException("Invalid type: " + type);
        }
        return typeName;
    }

    @Override
    public void registerType(String type, TypeName typeName) {
        typeToClassMapping.put(type, typeName);
    }

    @Override
    public CodeBlock getTypeConverter(String type, CodeBlock cb) {
        if (!typeConverterMapping.containsKey(type)) {
            throw new IllegalArgumentException("No converter found for type: " + type);
        }

        return typeConverterMapping.get(type).apply(cb);
    }
}


