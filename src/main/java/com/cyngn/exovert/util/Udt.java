package com.cyngn.exovert.util;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.UserType;
import com.google.common.base.CaseFormat;
import org.apache.commons.lang.StringUtils;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Util functions related to UDTs.
 *
 * @author truelove@cyngn.com (Jeremy Truelove) 8/28/15
 */
public class Udt {
    public static final Udt instance = new Udt();
    private KeyspaceMetadata keyspaceMetadata;
    private AtomicBoolean initialized;

    private Udt(){
        initialized = new AtomicBoolean(false);
    }

    public synchronized void init(KeyspaceMetadata keyspaceMetadata) {
        if(initialized.compareAndSet(false, true)) {
            this.keyspaceMetadata = keyspaceMetadata;
        }
    }

    public boolean isUdt(String name) {
        boolean is = false;
        for(UserType type : keyspaceMetadata.getUserTypes()) {
            if (name.toLowerCase().equals(type.getTypeName().toLowerCase())) {
                is = true;
                break;
            }
        }
        return is;
    }

    public boolean isUdt(DataType type) {
        return StringUtils.equalsIgnoreCase("udt", type.getName().name());
    }

    public String getUdtClassName(String udt) {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, udt);
    }
}
