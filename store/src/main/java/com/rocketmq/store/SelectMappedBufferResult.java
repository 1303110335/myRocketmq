/**
 * bianque.com
 * Copyright (C) 2013-2020 All Rights Reserved.
 */
package com.rocketmq.store;

import java.nio.ByteBuffer;

/**
 * 获取映射Buffer结果
 *
 * @author xuleyan
 * @version SelectMappedBufferResult.java, v 0.1 2020-11-23 3:07 下午
 */
public class SelectMappedBufferResult {
    /**
     * 引用mappedByteBuffer数据数组的一段
     */
    private final ByteBuffer byteBuffer;
    /**
     * buffer长度
     */
    private int size;
    /**
     * 映射文件
     */
    private MappedFile mappedFile;
    /**
     * 映射文件开始读取物理位置
     */
    private final long startOffset;

    public SelectMappedBufferResult(long startOffset, ByteBuffer byteBuffer, int size, MappedFile mappedFile) {
        this.startOffset = startOffset;
        this.byteBuffer = byteBuffer;
        this.size = size;
        this.mappedFile = mappedFile;
    }

    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    public int getSize() {
        return size;
    }

    public void setSize(final int s) {
        this.size = s;
        this.byteBuffer.limit(this.size);
    }

    public MappedFile getMappedFile() {
        return mappedFile;
    }

    public synchronized void release() {
        if (this.mappedFile != null) {
            this.mappedFile.release();
            this.mappedFile = null;
        }
    }

    public long getStartOffset() {
        return startOffset;
    }
}