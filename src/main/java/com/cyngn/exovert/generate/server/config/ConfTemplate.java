package com.cyngn.exovert.generate.server.config;

/**
 * sample conf.json template file, long term need to use a better templating solution.
 *
 * @author truelove@cyngn.com (Jeremy Truelove) 9/10/15
 */
public class ConfTemplate {
    public final static String TEMPLATE = "{\n" +
            "    \"port\" : 8080,\n" +
            "    \"cassandra\": {\n" +
            "        \"seeds\": [\"localhost\"],\n" +
            "        \"reconnect\": {\n" +
            "            \"name\": \"exponential\",\n" +
            "            \"base_delay\": 1000,\n" +
            "            \"max_delay\": 10000\n" +
            "        }\n" +
            "    }\n" +
            "}";
}
