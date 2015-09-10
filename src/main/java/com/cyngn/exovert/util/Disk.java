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
     * @throws IOException if write to file fails
     */
    public static void outputFile(JavaFile file) throws IOException {
        String javaFile = file.typeSpec.name + ".java";
        if (!isPreview()) {
            String path = MetaData.instance.getOutDir() + "/" + StringUtils.replace(file.packageName, ".", "/");
            String fileName = path + "/" + javaFile;

            writeFile(fileName, file.toString());
        } else {
            System.out.println("\nFile: " + javaFile + "\n");
            file.writeTo(System.out);
        }
    }

    /**
     * Outputs a generated JavaFile
     * @param fileData the file to output
     * @param fileName the name of the file
     * @throws IOException if write to file fails
     */
    public static void outputFile(String fileData, String fileName) throws IOException {
        if (!isPreview()) {

            String path = MetaData.instance.getOutDir();
            String fullPath = path + "/" + fileName;

            writeFile(fullPath, fileData);
        } else {
            System.out.println("\nFile: " + fileName + "\n");
            System.out.println(fileData);
        }
    }

    /**
     * Write the file out.
     *
     * @param path the path to write to
     * @param file the file data
     */
    private static void writeFile(String path, String file) {
        Vertx vertx = VertxRef.instance.get();
        if(!vertx.fileSystem().existsBlocking(path)) {
            vertx.fileSystem().mkdirsBlocking(path);
        }

        System.out.println("Outputting file to path: " + path);

        VertxRef.instance.get().fileSystem().writeFile(path, Buffer.buffer(file), result -> {
            if(result.failed()) {
                System.out.println("Failed to create file: " + path + ", ex: " + result.cause());
            }
        });
    }

    /**
     * @return Is the tool running in preview mode?
     */
    public static boolean isPreview() {
        return StringUtils.isEmpty(MetaData.instance.getOutDir());
    }
}
