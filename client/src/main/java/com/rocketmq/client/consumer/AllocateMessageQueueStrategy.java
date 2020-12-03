/**
 * bianque.com
 * Copyright (C) 2013-2020All Rights Reserved.
 */
package com.rocketmq.client.consumer;

import com.rocketmq.common.message.MessageQueue;

import java.util.List;

/**
 * 消息分配策略
 *
 * @author xuleyan
 * @version AllocateMessageQueueStrategy.java, v 0.1 2020-11-28 9:31 下午
 */
public interface AllocateMessageQueueStrategy {

    List<MessageQueue> allocate(
            final String consumerGroup,
            final String currentCID,
            final List<MessageQueue> mqAll,
            final List<String> cidAll);

    String getName();

}