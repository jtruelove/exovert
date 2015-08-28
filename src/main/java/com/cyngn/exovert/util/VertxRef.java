package com.cyngn.exovert.util;

import io.vertx.core.Vertx;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Util to get access to the current vert.x context.
 *
 * @author truelove@cyngn.com (Jeremy Truelove) 8/28/15
 */
public class VertxRef {
    public static final VertxRef instance = new VertxRef();
    private AtomicBoolean initialized;
    private Vertx vertx;

    private VertxRef(){
        initialized = new AtomicBoolean(false);
    }

    public synchronized void init(Vertx vertx) {
        if(initialized.compareAndSet(false, true)) {
            this.vertx = vertx;
        }
    }

    public Vertx get() { return vertx; }
}
