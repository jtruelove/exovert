package com.cyngn.exovert.generate.server.model.utils;

import com.cyngn.exovert.generate.server.rest.InterfaceSpec;
import com.cyngn.exovert.generate.server.rest.utils.RestGeneratorHelper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author asarda@cyngn.com (Ajay Sarda) 9/18/15.
 */
public class RestGeneratorHelperTest {

    @Test
    public void testGetApiName() {
        assertEquals("AbstractCreateBeerApi", RestGeneratorHelper.getApiName("createBeer"));
        assertEquals("AbstractCreateBeerApi", RestGeneratorHelper.getApiName("CreateBeer"));
    }

    @Test
    public void testGetRequestObjectName() {
        assertEquals("CreateBeerRequest", RestGeneratorHelper.getRequestObjectName("CreateBeer"));
        assertEquals("CreateBeerRequest", RestGeneratorHelper.getRequestObjectName("createBeer"));

        //TODO: fix this
        //assertEquals("CreateBeerRequest", RestGeneratorHelper.getRequestObjectName("create_beer"));

        // TODO: should i expect this
        //assertEquals("CreateBeerRequest", RestGeneratorHelper.getRequestObjectName("CREATEBEER"));
    }

    @Test
    public void testGetRequestVariableName() {
        assertEquals("createBeerRequest", RestGeneratorHelper.getRequestVariableName("CreateBeer"));
        assertEquals("createBeerRequest", RestGeneratorHelper.getRequestVariableName("createBeer"));

        //TODO: fix this
        //assertEquals("CreateBeerRequest", RestGeneratorHelper.getRequestObjectName("create_beer"));

        // TODO: should i expect this
        //assertEquals("CreateBeerRequest", RestGeneratorHelper.getRequestObjectName("CREATEBEER"));
    }

    @Test
    public void testGetSetMethodName() {
        assertEquals("setName", RestGeneratorHelper.getSetMethodName("name"));
        assertEquals("setBeerType", RestGeneratorHelper.getSetMethodName("beerType"));
    }

    @Test
    public void testGetGetMethodName() {
        assertEquals("getName", RestGeneratorHelper.getGetMethodName("name"));
        assertEquals("getBeerType", RestGeneratorHelper.getGetMethodName("beerType"));
    }

    @Test
    public void testGetFieldName() {
        assertEquals("name", RestGeneratorHelper.getFieldName("name"));
        assertEquals("beerType", RestGeneratorHelper.getFieldName("beer_type"));
        assertEquals("beerType", RestGeneratorHelper.getFieldName("beer_Type"));
        assertEquals("beerType", RestGeneratorHelper.getFieldName("Beer_Type"));

        //TODO: fix this
        //assertEquals("beerType", RestGeneratorHelper.getFieldName("beerType"));
    }

    @Test
    public void testGetTypeName() {
        assertEquals("String", RestGeneratorHelper.getTypeName("String"));
        assertEquals("BeerType", RestGeneratorHelper.getTypeName("beer_type"));
        assertEquals("List<String>", RestGeneratorHelper.getTypeName("List<String>"));
        assertEquals("List<BeerType>", RestGeneratorHelper.getTypeName("List<beer_type>"));
        assertEquals("Map<String,String>", RestGeneratorHelper.getTypeName("Map< String , String>"));
        assertEquals("Map<String,BeerType>", RestGeneratorHelper.getTypeName("Map< String , beer_type>"));
        assertEquals("Map<BeerType,String>", RestGeneratorHelper.getTypeName("Map< beer_type , String>"));
        assertEquals("Map<BeerType,BeerType>", RestGeneratorHelper.getTypeName("Map< beer_type , beer_type>"));
    }

    @Test
    public void testGetBuilderTypeName() {
        assertEquals("CreateBeerRequest.Builder", RestGeneratorHelper.getBuilderTypeName("CreateBeerRequest"));
    }

    @Test
    public void testGetBuilderVariableName() {
        assertEquals("builder", RestGeneratorHelper.getBuilderVariableName());
    }

    @Test
    public void testGetHandlerName() {
        assertEquals("handleGet", RestGeneratorHelper.getHandlerName("get"));
        assertEquals("handleGet", RestGeneratorHelper.getHandlerName("Get"));
        assertEquals("handleGet", RestGeneratorHelper.getHandlerName("GET"));
        assertEquals("handlePost", RestGeneratorHelper.getHandlerName("post"));
        assertEquals("handlePost", RestGeneratorHelper.getHandlerName("Post"));
        assertEquals("handlePost", RestGeneratorHelper.getHandlerName("POST"));
        assertEquals("handleDelete", RestGeneratorHelper.getHandlerName("delete"));
        assertEquals("handleDelete", RestGeneratorHelper.getHandlerName("Delete"));
        assertEquals("handleDelete", RestGeneratorHelper.getHandlerName("DELETE"));
    }

    @Test
    public void testLoadTypeSpecFromFile() throws Exception {
        // find better way of doing this
        InterfaceSpec spec = RestGeneratorHelper.loadSpecFromFile("src/test/java/com/cyngn/exovert/generate/server/model/utils/api.json");

        assertEquals(1, spec.apis.size());
        assertEquals(1, spec.dataTypes.enumTypes.size());
        assertEquals(1, spec.dataTypes.classTypes.size());
    }
}