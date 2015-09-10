package com.cyngn.exovert.generate.server.config;

/**
 *  logback.xml template file, long term need to use a better templating solution.
 *
 * @author truelove@cyngn.com (Jeremy Truelove) 9/10/15
 */
public class LogbackTemplate {

    public final static String TEMPLATE = "<configuration>\n" +
            "    <appender name=\"STDOUT\" class=\"ch.qos.logback.core.ConsoleAppender\">\n" +
            "        <!-- encoders are assigned the type\n" +
            "             ch.qos.logback.classic.encoder.PatternLayoutEncoder by default -->\n" +
            "        <encoder>\n" +
            "            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>\n" +
            "        </encoder>\n" +
            "    </appender>\n" +
            "\n" +
            "    <root level=\"INFO\">\n" +
            "        <appender-ref ref=\"STDOUT\" />\n" +
            "    </root>\n" +
            "</configuration>";
}
