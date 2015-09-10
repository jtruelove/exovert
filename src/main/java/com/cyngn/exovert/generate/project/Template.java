package com.cyngn.exovert.generate.project;

/**
 * build.gradle template file, long term need to use a better templating solution.
 *
 * @author truelove@cyngn.com (Jeremy Truelove) 9/9/15
 */
public class Template {
    public static String data = "buildscript {\n" +
            "    repositories { jcenter() }\n" +
            "    dependencies {\n" +
            "        classpath 'com.github.jengelman.gradle.plugins:shadow:1.1.1'\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "apply plugin: 'java'\n" +
            "apply plugin: 'com.github.johnrengelman.shadow'\n" +
            "\n" +
            "version = '0.1.0'\n" +
            "serverClass = '%s'\n" +
            "group = '%s'\n" +
            "archivesBaseName = '%s'\n" +
            "\n" +
            "if (!JavaVersion.current().java8Compatible) {\n" +
            "    throw new IllegalStateException('''A Haiku:\n" +
            "                                      |  This needs Java 8,\n" +
            "                                      |  You are using something else,\n" +
            "                                      |  Refresh. Try again.'''.stripMargin())\n" +
            "}\n" +
            "\n" +
            "repositories {\n" +
            "    mavenCentral()\n" +
            "    maven { url = 'http://oss.sonatype.org/content/repositories/snapshots/' }\n" +
            "    maven { url = 'http://oss.sonatype.org/content/repositories/releases/' }\n" +
            "}\n" +
            "\n" +
            "dependencies {\n" +
            "    compile 'io.vertx:vertx-core:3.0.0'\n" +
            "    compile \"joda-time:joda-time:2.4\"\n" +
            "    compile \"com.google.guava:guava:18.0\"\n" +
            "    compile \"commons-lang:commons-lang:2.6\"\n" +
            "    compile \"net.sf.jopt-simple:jopt-simple:4.9\"\n" +
            "    compile \"com.cyngn.vertx:vertx-util:0.5.4\"\n" +
            "    compile \"com.englishtown.vertx:vertx-cassandra:3.0.0\"\n" +
            "    compile \"com.englishtown.vertx:vertx-cassandra-mapping:3.0.0\"\n" +
            "    compile \"ch.qos.logback:logback-classic:1.0.13\"\n" +
            "    compile \"ch.qos.logback:logback-core:1.0.13\"\n" +
            "    testCompile \"junit:junit:4.11\"\n" +
            "    testCompile \"io.vertx:vertx-unit:3.0.0\"\n" +
            "}\n" +
            "\n" +
            "task wrapper(type: Wrapper) {\n" +
            "    gradleVersion = '2.0'\n" +
            "}\n" +
            "\n" +
            "task release() << {}\n" +
            "\n" +
            "gradle.taskGraph.whenReady {taskGraph ->\n" +
            "    if (!taskGraph.hasTask(release)) {\n" +
            "        version += '-SNAPSHOT'\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "task javadocJar(type: Jar) {\n" +
            "    classifier = 'javadoc'\n" +
            "    from javadoc\n" +
            "}\n" +
            "\n" +
            "task sourcesJar(type: Jar) {\n" +
            "    classifier = 'sources'\n" +
            "    from sourceSets.main.allSource\n" +
            "}\n" +
            "\n" +
            "artifacts {\n" +
            "    archives javadocJar, sourcesJar\n" +
            "}\n" +
            "\n" +
            "shadowJar {\n" +
            "    classifier = 'fat'\n" +
            "    manifest {\n" +
            "        attributes 'Main-Class': 'io.vertx.core.Starter'\n" +
            "        attributes 'Main-Verticle': serverClass\n" +
            "    }\n" +
            "    mergeServiceFiles {\n" +
            "        include 'META-INF/services/io.vertx.core.spi.VerticleFactory'\n" +
            "    }\n" +
            "}";
}
