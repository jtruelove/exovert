package com.cyngn.exovert.generate.server.rest;

import com.cyngn.exovert.generate.server.rest.utils.RestGeneratorHelper;

/**
 * Stores context metadata around generation run
 *
 * @author asarda@cyngn.com (Ajay Sarda) 9/14/15.
 */
class GenerationContext {
    public TypeMap typeMap;

    // setting default context
    public boolean preview = false;

    public String outputDirectory =
            RestGeneratorHelper.getGeneratedSourceDirectory();

    public String specFilePath = "api.json";
}
