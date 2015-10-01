package com.cyngn.exovert.generate.server.model;

import com.cyngn.exovert.generate.server.rest.TypeMap;
import com.google.common.base.CaseFormat;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

/**
 * @author asarda@cyngn.com (Ajay Sarda) 9/18/15.
 */
public class TypeMapImplTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void testRegisterType(){
        TypeMap typeMap = TypeMap.create();
        typeMap.registerType("T", ClassName.get("", "T"));

        assertEquals("T", typeMap.getTypeName("T").toString());
    }

    @Test
    public void testAlreadyRegister(){
        TypeMap typeMap = TypeMap.create();
        typeMap.registerType("T", ClassName.get("", "T"));

        exception.expect(IllegalArgumentException.class);
        typeMap.registerType("T", ClassName.get("", "T"));
    }

    @Test
    public void testTypeConverter() {
        TypeMap typeMap = TypeMap.create();

        CodeBlock cb = typeMap.getTypeConverter("String",
                CodeBlock.builder().add("request.getParam($S)", CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, "name")).build());

        assertEquals("request.getParam(\"name\")", cb.toString());

        cb = typeMap.getTypeConverter("Long",
                CodeBlock.builder().add("request.getParam($S)", CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, "value")).build());

        assertEquals("java.lang.Long.parseLong(request.getParam(\"value\"))", cb.toString());
    }

}