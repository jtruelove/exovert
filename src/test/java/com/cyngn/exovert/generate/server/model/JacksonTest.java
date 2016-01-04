package com.cyngn.exovert.generate.server.model;

import com.cyngn.vertx.web.JsonUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author andy@cyngn.com (Andy Mast) 1/4/16
 */
public class JacksonTest {

    @Test
    public void testFooParsed() {
        String json = "{\n" +
                "  \"fieldRequired\": \"required\",\n" +
                "  \"fieldOptional\": \"present\"\n" +
                "}";

        Foo foo = JsonUtil.parseJsonToObject(json, Foo.class);
        assertNotNull(foo);
        assertNotNull(foo.fieldRequired);
        assertNotNull(foo.fieldOptional);
    }

    @Test
    public void testOptionalParsedWithNull() {
        String json = "{\n" +
                "  \"fieldRequired\": \"required\",\n" +
                "  \"fieldOptional\": null\n" +
                "}";

        Foo foo = JsonUtil.parseJsonToObject(json, Foo.class);
        assertNotNull(foo);
        assertNotNull(foo.fieldRequired);
        assertNull(foo.fieldOptional);
    }

    @Test
    public void testFooParsedWithMissingField() {
        String json = "{\n" +
                "  \"fieldRequired\": \"required\"\n" +
                "}";

        Foo foo = JsonUtil.parseJsonToObject(json, Foo.class);
        assertNotNull(foo);
        assertNotNull(foo.fieldRequired);
        assertNull(foo.fieldOptional);
    }

    public static class Foo {
        @JsonProperty("fieldRequired")
        public String fieldRequired;

        @JsonProperty("fieldOptional")
        public String fieldOptional;
    }
}
