package com.cyngn.exovert.generate.rest;

import com.cyngn.exovert.util.Disk;
import com.cyngn.exovert.util.GeneratorHelper;
import com.cyngn.exovert.util.MetaData;
import com.cyngn.vertx.web.RestApi;
import com.datastax.driver.core.TableMetadata;
import com.google.common.base.CaseFormat;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.Collection;

/**
 * @author truelove@cyngn.com (Jeremy Truelove) 9/1/15
 */
public class RestGenerator {

    public static void generate(Collection<TableMetadata> tables) throws IOException {
        String namespaceToUse = MetaData.instance.getRestNamespace();

        for (TableMetadata table : tables) {
            String rawName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, table.getName());
            String name = rawName + "Api";

            ClassName entityTable = ClassName.get(MetaData.instance.getTableNamespace(), rawName);

            TypeSpec.Builder restBuilder = TypeSpec.classBuilder(name)
                    .addModifiers(Modifier.PUBLIC);

            restBuilder.addField(GeneratorHelper.getLogger(namespaceToUse, name));

            restBuilder.addJavadoc(MetaData.getJavaDocHeader("REST Api for Cassandra entity - " + rawName));

            restBuilder.addSuperinterface(RestApi.class);

            JavaFile javaFile = JavaFile.builder(namespaceToUse, restBuilder.build()).build();

            Disk.outputFile(javaFile);
        }
    }
}
