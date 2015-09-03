package com.cyngn.exovert.util;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.Modifier;

/**
 * @author truelove@cyngn.com (Jeremy Truelove) 9/1/15
 */
public class GeneratorHelper {
    public static FieldSpec getLogger(String nameSpace, String className) {
        return FieldSpec.builder(Logger.class, "logger", Modifier.FINAL, Modifier.STATIC, Modifier.PRIVATE)
                .initializer("$T.getLogger($T.class)", LoggerFactory.class, ClassName.get(nameSpace, className)).build();
    }
}
