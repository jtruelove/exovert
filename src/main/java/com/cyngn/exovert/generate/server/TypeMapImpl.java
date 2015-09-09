package com.cyngn.exovert.generate.server;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link TypeMap}
 *
 * Coupled with {@link TypeName} to keep knowledge of types that are fed into
 * code generation.
 *
 * @author asarda@cyngn.com (Ajay Sarda) 9/8/15.
 */
public class TypeMapImpl implements TypeMap {

    Map<String, TypeName> typeToClassMapping = new HashMap<>();

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


        typeToClassMapping.put("String", ClassName.get("java.lang", "String"));

        //TODO: add list, map types.
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
}


