/**
 * bianque.com
 * Copyright (C) 2013-2020 All Rights Reserved.
 */
package com.rocketmq.store;

import com.rocketmq.common.UtilAll;
import com.sun.deploy.cache.BaseLocalApplicationProperties;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 性能分析
 * 从代码层面上看，从硬盘上将文件读入内存，都要经过文件系统进行数据拷贝，并且数据拷贝操作是由文件系统和硬件驱动实现的，理论上来说，拷贝数据的效率是一样的。
 * 但是通过内存映射的方法访问硬盘上的文件，效率要比read和write系统调用高，这是为什么？
 * <p>
 * read()是系统调用，首先将文件从硬盘拷贝到内核空间的一个缓冲区，再将这些数据拷贝到用户空间，实际上进行了两次数据拷贝；
 * map()也是系统调用，但没有进行数据拷贝，当缺页中断发生时，直接将文件从硬盘拷贝到用户空间，只进行了一次数据拷贝。
 * 所以，采用内存映射的读写效率要比传统的read/write性能高。
 *
 * @author xuleyan
 * @version MappedFile.java, v 0.1 2020-11-23 2:02 下午
 */
@Slf4j
public class MappedFile extends ReferenceResource {

    private String fileName;

    private int fileSize;
    /**
     * 文件
     */
    private File file;
    /**
     * 文件开始的offset
     */
    private Long fileFromOffset;
    /**
     * fileChannel
     */
    private FileChannel fileChannel;
    /**
     * 文件映射buffer
     */
    private MappedByteBuffer mappedByteBuffer;
    /**
     * 当前追加到MappedByteBuffer的位置
     */
    protected final AtomicInteger wrotePosition = new AtomicInteger(0);
    /**
     * 映射虚拟内存总字节数
     */
    private static final AtomicLong TOTAL_MAPPED_VIRTUAL_MEMORY = new AtomicLong(0);
    /**
     * 映射文件总数
     */
    private static final AtomicInteger TOTAL_MAPPED_FILES = new AtomicInteger(0);
    /**
     * Message will put to here first, and then reput to FileChannel if writeBuffer is not null.
     * 写入缓冲
     */
    protected ByteBuffer writeBuffer = null;
    /**
     * 当前flush位置
     */
    private final AtomicInteger flushedPosition = new AtomicInteger(0);
    /**
     * 当前commit位置
     */
    private final AtomicInteger committedPosition = new AtomicInteger(0);

    public int getWrotePosition() {
        return wrotePosition.get();
    }

    public AtomicInteger getFlushedPosition() {
        return flushedPosition;
    }

    public MappedFile(final String fileName, final int fileSize) throws IOException {
        init(fileName, fileSize);
    }

    private void init(final String fileName, final int fileSize) throws IOException {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.file = new File(fileName);
        this.fileFromOffset = Long.parseLong(this.file.getName());

        boolean ok = false;
        ensureDirOk(this.file.getParent());

        try {
            this.fileChannel = new RandomAccessFile(this.file, "rw").getChannel();
            this.mappedByteBuffer = this.fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, fileSize);
            TOTAL_MAPPED_VIRTUAL_MEMORY.addAndGet(fileSize);
            TOTAL_MAPPED_FILES.incrementAndGet();
            ok = true;
        } catch (FileNotFoundException e) {
            log.error("create file channel " + this.fileName + " Failed. ", e);
            throw e;
        } catch (IOException e) {
            log.error("map file " + this.fileName + " Failed. ", e);
            throw e;
        } finally {
            if (!ok && fileChannel != null) {
                fileChannel.close();
            }
        }
    }

    private void ensureDirOk(final String dirName) {
        if (dirName != null) {
            File file = new File(dirName);
            if (!file.exists()) {
                boolean result = file.mkdirs();
                log.info(dirName + " mkdir " + (result ? "OK" : "Failed"));
            }
        }
    }

    /**
     * @param data
     * @return
     */
    public boolean appendMessage(final byte[] data) {
        int currentPosition = this.wrotePosition.get();
        if ((currentPosition + data.length) <= this.fileSize) {
            try {
                this.fileChannel.position(currentPosition);
                this.fileChannel.write(ByteBuffer.wrap(data));
            } catch (IOException e) {
                log.error("Error occurred when append message to mappedFile.", e);
            }
            this.wrotePosition.addAndGet(data.length);
            return true;
        }
        return false;
    }

    /**
     * @param data
     * @return
     */
    public boolean appendMessage2(final byte[] data) {
        int currentPosition = this.wrotePosition.get();
        if ((currentPosition + data.length) <= this.fileSize) {
            int length = data.length + 16;
            try {
                ByteBuffer byteBuffer = this.mappedByteBuffer.slice();
                byteBuffer.position(currentPosition);

                ByteBuffer buffer = ByteBuffer.allocate(1024);
                buffer.put(data);
                buffer.putInt(1024);
                buffer.putInt(12345678);
                buffer.putLong(234L);

                byteBuffer.put(buffer.array(), 0, length);

            } catch (Exception e) {
                log.error("Error occurred when append message to mappedFile.", e);
            }
            this.wrotePosition.addAndGet(length);
            return true;
        }
        return false;
    }


    @Override
    protected boolean cleanUp(long currentRef) {
        if (this.isAvailable()) {
            log.error("this file[REF:" + currentRef + "] " + this.fileName
                    + " have not shutdown, stop unmaping.");
            return false;
        }

        if (this.isCleanupOver()) {
            log.error("this file[REF:" + currentRef + "] " + this.fileName
                    + " have cleanup, do not do it again.");
            return true;
        }

        clean(this.mappedByteBuffer);
        TOTAL_MAPPED_VIRTUAL_MEMORY.addAndGet(this.fileSize * (-1));
        TOTAL_MAPPED_FILES.decrementAndGet();
        log.info("unmap file[REF:" + currentRef + "] " + this.fileName + " OK");
        return true;
    }

    public static void clean(final ByteBuffer buffer) {
        if (buffer == null || !buffer.isDirect() || buffer.capacity() == 0) {
            return;
        }
        invoke(invoke(viewed(buffer), "cleaner"), "clean");
    }

    private static ByteBuffer viewed(ByteBuffer buffer) {
        String methodName = "viewedBuffer";

        Method[] methods = buffer.getClass().getMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals("attachment")) {
                methodName = "attachment";
                break;
            }
        }

        ByteBuffer viewedBuffer = (ByteBuffer) invoke(buffer, methodName);
        if (viewedBuffer == null) {
            return buffer;
        } else {
            return viewed(viewedBuffer);
        }
    }

    private static Object invoke(final Object target, final String methodName, final Class<?>... args) {
        return AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                try {
                    Method method = method(target, methodName, args);
                    method.setAccessible(true);
                    return method.invoke(target);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }
        });
    }

    private static Method method(Object target, String methodName, Class<?>[] args)
            throws NoSuchMethodException {
        try {
            return target.getClass().getMethod(methodName, args);
        } catch (NoSuchMethodException e) {
            return target.getClass().getDeclaredMethod(methodName, args);
        }
    }

    public SelectMappedBufferResult selectMappedBuffer(int pos) {
        int readPosition = getReadPosition();
        if (pos < readPosition && pos >= 0) {
            if (this.hold()) {
                ByteBuffer byteBuffer = this.mappedByteBuffer.slice();
                byteBuffer.position(pos);
                int size = readPosition - pos;
                ByteBuffer byteBufferNew = byteBuffer.slice();
                byteBufferNew.limit(size);
                return new SelectMappedBufferResult(this.fileFromOffset + pos, byteBufferNew, size, this);
            }
        }

        return null;
    }

    private int getReadPosition() {
        return this.writeBuffer == null ? this.wrotePosition.get() : this.committedPosition.get();
    }

    public boolean destroy(final long intervalForcibly) {
        this.shutdown(intervalForcibly);

        if (this.isCleanupOver()) {
            try {
                this.fileChannel.close();
                log.info("close file channel " + this.fileName + " OK");

                long beginTime = System.currentTimeMillis();
                boolean result = this.file.delete();
                log.info("delete file[REF:" + this.getRefCount() + "] " + this.fileName
                        + (result ? " OK, " : " Failed, ") + "W:" + this.getWrotePosition() + " M:"
                        + this.getFlushedPosition() + ", "
                        + UtilAll.computeEclipseTimeMilliseconds(beginTime));
            } catch (Exception e) {
                log.warn("close file channel " + this.fileName + " Failed. ", e);
            }

            return true;
        } else {
            log.warn("destroy mapped file[REF:" + this.getRefCount() + "] " + this.fileName
                    + " Failed. cleanupOver: " + this.cleanupOver);
        }

        return false;
    }

}