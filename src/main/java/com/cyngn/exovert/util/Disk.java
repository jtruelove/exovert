package com.cyngn.exovert.util;

import com.squareup.javapoet.JavaFile;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;

/**
 * Wrapper for interacting with disk and outputting data.
 *
 * @author truelove@cyngn.com (Jeremy Truelove) 8/28/15
 */
public class Disk {
    /**
     * Outputs a generated JavaFile
     * @param file the file to output
     * @throws IOException if write to the file failes
     */
    public static void outputFile(JavaFile file) throws IOException {
        if (!isPreview()) {
            Vertx vertx = VertxRef.instance.get();

            String path = MetaData.instance.getOutDir() + "/" + StringUtils.replace(file.packageName, ".", "/");
            String fileName = path + "/" + file.typeSpec.name + ".java";

            if(!vertx.fileSystem().existsBlocking(path)) {
                vertx.fileSystem().mkdirsBlocking(path);
            }

            System.out.println("Outputting file to path: " + fileName);

            VertxRef.instance.get().fileSystem().writeFile(fileName, Buffer.buffer(file.toString()), result -> {
                if(result.failed()) {
                    System.out.println("Failed to create file: " + path + ", ex: " + result.cause());
                }
            });
        } else {
            file.writeTo(System.out);
        }
    }

    /**
     * @return Is the tool running in preview mode?
     */
    public static boolean isPreview() {
        return StringUtils.isEmpty(MetaData.instance.getOutDir());
    }
}
