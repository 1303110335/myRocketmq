/**
 * bianque.com
 * Copyright (C) 2013-2020 All Rights Reserved.
 */
package com.rocketmq.store;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author xuleyan
 * @version ReferenceResource.java, v 0.1 2020-11-23 3:12 下午
 */
public abstract class ReferenceResource {

    protected final AtomicLong refCount = new AtomicLong(1);
    protected volatile boolean available = true;
    protected volatile boolean cleanupOver = false;
    private volatile long firstShutdownTimestamp = 0;

    /**
     * 标识连接资源的持有
     *
     * @return
     */
    public synchronized boolean hold() {
        if (isAvailable()) {
            if (this.refCount.getAndIncrement() > 0) {
                return true;
            } else {
                this.refCount.getAndDecrement();
            }
        }

        return false;
    }

    /**
     * 标识连接资源的释放
     */
    public void release() {
        long value = this.refCount.decrementAndGet();
        if (value > 0) {
            return;
        }
        synchronized (this) {
            this.cleanupOver = this.cleanUp(value);
        }
    }

    protected abstract boolean cleanUp(final long currentRef);

    public long getRefCount() {
        return refCount.get();
    }

    public boolean isAvailable() {
        return available;
    }

    public boolean isCleanupOver() {
        return cleanupOver;
    }

    public long getFirstShutdownTimestamp() {
        return firstShutdownTimestamp;
    }

    public void shutdown(final long intervalForcibly) {
        if (this.isAvailable()) {
            this.available = false;
            this.firstShutdownTimestamp = System.currentTimeMillis();
            this.release();
        } else if (this.getRefCount() > 0) {
            if ((System.currentTimeMillis() - this.firstShutdownTimestamp) >= intervalForcibly) {
                this.refCount.set(-1000 - this.getRefCount());
                this.release();
            }
        }
    }
}