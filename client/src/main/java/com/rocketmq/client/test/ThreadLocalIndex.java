/**
 * bianque.com
 * Copyright (C) 2013-2020 All Rights Reserved.
 */
package com.rocketmq.client.test;

import java.util.Random;

/**
 * @author xuleyan
 * @version ThreadLocalIndex.java, v 0.1 2020-11-26 11:15 下午
 */
public class ThreadLocalIndex {

    private final ThreadLocal<Integer> threadLocalIndex = new ThreadLocal<>();

    private final Random random = new Random();

    public int getAndIncrement() {
        Integer index = this.threadLocalIndex.get();
        if (index == null) {
            index = Math.abs(random.nextInt());
            if (index < 0) {
                index = 0;
            }
            this.threadLocalIndex.set(index);
        }

        index = Math.abs(index + 1);
        if (index < 0) {
            index = 0;
        }

        this.threadLocalIndex.set(index);
        return index;
    }

    @Override
    public String toString() {
        return "ThreadLocalIndex{" +
                "threadLocalIndex=" + threadLocalIndex +
                ", random=" + random +
                '}';
    }

}