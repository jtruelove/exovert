package com.cyngn.exovert.generate.project;

import com.cyngn.exovert.util.Disk;
import com.cyngn.exovert.util.MetaData;

import java.io.IOException;

/**
 * Handle generating project gradle files.
 *
 * @author truelove@cyngn.com (Jeremy Truelove) 9/9/15
 */
public class GradleGenerator {

    /**
     * Handles generating project gradle files
     *
     * @param projectName the name of the project
     * @throws IOException thrown if can't write to disk
     */
    public static void generate(String projectName) throws IOException {
        String namespace = MetaData.instance.getNamespace();
        String file = String.format(Template.data, namespace + ".Server", namespace, projectName);

        Disk.outputFile(file, "build.gradle");
    }
}
