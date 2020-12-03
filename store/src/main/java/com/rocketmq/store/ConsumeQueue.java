/**
 * bianque.com
 * Copyright (C) 2013-2020 All Rights Reserved.
 */
package com.rocketmq.store;

/**
 * @author xuleyan
 * @version ConsumeQueue.java, v 0.1 2020-10-29 8:44 下午
 */
public class ConsumeQueue {

    public static final int CQ_STORE_UNIT_SIZE = 20;

    /**
     * Topic
     */
    private final String topic;
    /**
     * 队列编号
     */
    private final int queueId;

    public ConsumeQueue(String topic, int queueId) {
        this.topic = topic;
        this.queueId = queueId;
    }
}