package com.cyngn.exovert.generate.entity;

import com.squareup.javapoet.TypeName;

/**
 * @author truelove@cyngn.com (Jeremy Truelove) 8/28/15
 */
class TypeResult {
    public TypeName type;
    public boolean hasFrozenType;

    public TypeResult(TypeName type, boolean hasFrozenType) {
        this.type = type;
        this.hasFrozenType = hasFrozenType;
    }
}
