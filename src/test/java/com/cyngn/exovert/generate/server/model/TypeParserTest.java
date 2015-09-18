package com.cyngn.exovert.generate.server.model;


import com.cyngn.exovert.generate.server.rest.TypeMap;
import com.cyngn.exovert.generate.server.rest.TypeParser;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests {@link TypeParser}
 *
 * @author asarda@cyngn.com (Ajay Sarda) 9/18/15.
 */
public class TypeParserTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void testIsList() {
        assertTrue(TypeParser.isList("List<String>"));
        assertTrue(TypeParser.isList("List< String >"));
        assertFalse(TypeParser.isList("NotAList<String>"));
        assertFalse(TypeParser.isList("String"));
    }

    @Test
    public void testGetListType() {
        assertEquals("String", TypeParser.getListType("List<String>"));
        assertEquals("T", TypeParser.getListType("List<T>"));

        exception.expect(IllegalArgumentException.class);
        TypeParser.getListType("NotAList<T>");

        exception.expect(IllegalArgumentException.class);
        TypeParser.getListType("NotAList<>");
    }

    @Test
    public void testIsMap() {
        assertTrue(TypeParser.isMap("Map<String,String>"));
        assertTrue(TypeParser.isMap("Map< String, String>"));
        assertTrue(TypeParser.isMap("Map< String , String >"));
        assertTrue(TypeParser.isMap("Map< K , V >"));
        assertTrue(TypeParser.isMap("Map< K , V >"));

        // TODO: fix this
        //Assert.assertFalse(TypeParser.isMap("Map<>"));
    }

    @Test
    public void testGetMapKeyType() {
        assertEquals("String", TypeParser.getMapKeyType("Map<String,String>"));
        assertEquals("K", TypeParser.getMapKeyType("Map<K,V>"));

        exception.expect(IllegalArgumentException.class);
        TypeParser.getMapKeyType("Map<>");

        exception.expect(IllegalArgumentException.class);
        TypeParser.getMapKeyType("Map<K>");

        exception.expect(IllegalArgumentException.class);
        TypeParser.getMapKeyType("Map<V>");

        exception.expect(IllegalArgumentException.class);
        TypeParser.getMapKeyType("Map<,>");
    }

    @Test
    public void testGetMapValueType() {
        assertEquals("String", TypeParser.getMapValueType("Map<String,String>"));
        assertEquals("V", TypeParser.getMapValueType("Map<K,V>"));

        exception.expect(IllegalArgumentException.class);
        TypeParser.getMapKeyType("Map<>");

        exception.expect(IllegalArgumentException.class);
        TypeParser.getMapKeyType("Map<K>");

        exception.expect(IllegalArgumentException.class);
        TypeParser.getMapKeyType("Map<V>");

        exception.expect(IllegalArgumentException.class);
        TypeParser.getMapKeyType("Map<,>");
    }

    @Test
    public void testParse() {
        TypeMap typeMap = TypeMap.create();
        TypeName typeName = TypeParser.parse("List<String>", typeMap);

        assertEquals("java.util.List<java.lang.String>", typeName.toString());

        typeMap.registerType("T", ClassName.get("", "T"));

        typeName = TypeParser.parse("List<T>", typeMap);
        assertEquals("java.util.List<T>", typeName.toString());

        typeName = TypeParser.parse("Map<String, String>", typeMap);

        assertEquals("java.util.Map<java.lang.String, java.lang.String>", typeName.toString());

        typeMap.registerType("K", ClassName.get("", "K"));
        typeMap.registerType("V", ClassName.get("", "V"));

        typeName = TypeParser.parse("Map<K, V>", typeMap);

        assertEquals("java.util.Map<K, V>", typeName.toString());
    }

}