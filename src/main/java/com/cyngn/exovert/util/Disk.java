package com.cyngn.exovert.util;

import com.squareup.javapoet.JavaFile;
import io.vertx.core.buffer.Buffer;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;

/**
 * @author truelove@cyngn.com (Jeremy Truelove) 8/28/15
 */
public class Disk {
    public static void outputFile(JavaFile file) throws IOException {
        if (!isPreview()) {
            VertxRef.instance.get().fileSystem().writeFile(MetaData.instance.getOutDir() + "/" + file.packageName + "/" + file.typeSpec.name, Buffer.buffer(file.toString()), Void -> {});
        } else {
            file.writeTo(System.out);
        }
    }

    public static boolean isPreview() {
        return StringUtils.isEmpty(MetaData.instance.getOutDir());
    }
}
