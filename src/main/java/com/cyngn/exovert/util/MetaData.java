package com.cyngn.exovert.util;

import com.datastax.driver.core.UserType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Util class for getting access to common meta data at run time.
 *
 * @author truelove@cyngn.com (Jeremy Truelove) 8/28/15
 */
public class MetaData {
    public static final MetaData instance = new MetaData();
    private AtomicBoolean initialized;
    private String namespace;
    private String keyspace;
    private String outDir;
    private DateTime updateTime;
    private String prefix;

    private static final String DEFAULT_API_PREFIX = "/api/v1/";

    private MetaData(){
        initialized = new AtomicBoolean(false);
    }

    public synchronized void init(String namespace, String keyspace, String outDir, String restPrefix) {
        if(initialized.compareAndSet(false, true)) {
            this.namespace = namespace;
            this.keyspace = keyspace;
            this.outDir = outDir;
            updateTime = DateTime.now(DateTimeZone.UTC);

            if (StringUtils.isNotEmpty(restPrefix)) { prefix = restPrefix + (restPrefix.endsWith("/") ? "" : "/");
            } else { prefix = DEFAULT_API_PREFIX; }
        }
    }

    public String getNamespace() { return namespace; }
    public String getKeyspace() { return keyspace; }
    public String getOutDir() { return outDir; }
    public String getUdtNamespace() { return StringUtils.join(new String[]{namespace, "storage", "cassandra", "udt"}, '.'); }
    public String getTableNamespace() { return StringUtils.join(new String[]{namespace, "storage", "cassandra", "table"}, '.'); }
    public String getDalNamespace() { return StringUtils.join(new String[]{namespace, "storage", "cassandra", "dal"}, '.'); }
    public String getRestNamespace() { return StringUtils.join(new String[]{namespace, "rest"}, '.'); }
    public String getRestPrefix() { return prefix; }
    public DateTime getUpdateTime() { return updateTime; }

    public static boolean isSnakeCase(String str) {
        return str.contains("_");
    }

    public static AnnotationSpec getJsonAnnotation(String field) {
        AnnotationSpec.Builder builder = AnnotationSpec.builder(JsonProperty.class);
        if(isSnakeCase(field)) {
            builder.addMember("value", "$S", field);
        }

        return builder.build();
    }

    public static ClassName getClassNameForUdt(UserType type) {
        return ClassName.get(MetaData.instance.getUdtNamespace(), Udt.instance.getUdtClassName(type.getTypeName()));
    }
}
