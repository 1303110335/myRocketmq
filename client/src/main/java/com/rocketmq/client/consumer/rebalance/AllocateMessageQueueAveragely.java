/**
 * bianque.com
 * Copyright (C) 2013-2020 All Rights Reserved.
 */
package com.rocketmq.client.consumer.rebalance;

import com.rocketmq.client.consumer.AllocateMessageQueueStrategy;
import com.rocketmq.common.message.MessageQueue;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Average Hashing queue algorithm
 * 队列分配策略 - 平均分配
 * 如果 队列数 和 消费者数量 相除有余数时，余数按照顺序"1"个"1"个分配消费者。
 * 例如，5个队列，3个消费者时，分配如下：
 * - 消费者0：[0, 1] 2个队列
 * - 消费者1：[2, 3] 2个队列
 * - 消费者2：[4, 4] 1个队列
 * <p>
 * 代码块 (mod > 0 && index < mod) 判断即在处理相除有余数的情况。
 */
@Slf4j
public class AllocateMessageQueueAveragely implements AllocateMessageQueueStrategy {

    @Override
    public List<MessageQueue> allocate(String consumerGroup, String currentCID, List<MessageQueue> mqAll, List<String> cidAll) {
        // 校验参数是否正确
        if (currentCID == null || currentCID.length() < 1) {
            throw new IllegalArgumentException("currentCID is empty");
        }
        if (mqAll == null || mqAll.isEmpty()) {
            throw new IllegalArgumentException("mqAll is null or mqAll empty");
        }
        if (cidAll == null || cidAll.isEmpty()) {
            throw new IllegalArgumentException("cidAll is null or cidAll empty");
        }

        List<MessageQueue> result = new ArrayList<>();
        if (!cidAll.contains(currentCID)) {
            log.info("[BUG] ConsumerGroup: {} The consumerId: {} not in cidAll: {}",
                    consumerGroup,
                    currentCID,
                    cidAll);
            return result;
        }
        // 平均分配
        // 第几个consumer。
        int index = cidAll.indexOf(currentCID);
        // 余数，即多少消息队列无法平均分配。
        int mod = mqAll.size() % cidAll.size();

        //队列总数 <= 消费者总数时，分配当前消费者1个队列
        //不能均分 &&  当前消费者序号(从0开始) < 余下的队列数 ，分配当前消费者 mqAll / cidAll +1 个队列
        //不能均分 &&  当前消费者序号(从0开始) >= 余下的队列数 ,分配当前消费者 mqAll / cidAll 个队列
        int averageSize = mqAll.size() <= cidAll.size() ? 1 : (mod > 0 && index < mod ? (mqAll.size() / cidAll.size()) + 1 : (mqAll.size() / cidAll.size()));

        // 开始分配的队列起始数：有余数的情况下，[0, mod) 平分余数，即每consumer多分配一个节点；第index开始，跳过前mod余数。
        int startIndex = (mod > 0 && index < mod) ? index * averageSize : index * averageSize + mod;
        // 分配队列数量。之所以要Math.min()的原因是，mqAll.size() <= cidAll.size()，部分consumer分配不到消费队列。
        int range = Math.min(averageSize, mqAll.size() - startIndex);
        for (int i = 0; i < range; i++) {
            result.add(mqAll.get((startIndex + i) % mqAll.size()));
        }
        return result;
    }

    public static void main(String[] args) {

        List<MessageQueue> messageQueueList = new ArrayList<>();
        MessageQueue messageQueue = new MessageQueue();
        messageQueue.setQueueId(1);
        messageQueueList.add(messageQueue);
        MessageQueue messageQueue2 = new MessageQueue();
        messageQueue2.setQueueId(2);
        messageQueueList.add(messageQueue2);

        MessageQueue messageQueue3 = new MessageQueue();
        messageQueue3.setQueueId(3);
        messageQueueList.add(messageQueue3);

        MessageQueue messageQueue4 = new MessageQueue();
        messageQueue4.setQueueId(4);
        messageQueueList.add(messageQueue4);

        AllocateMessageQueueAveragely allocateMessageQueueAveragely = new AllocateMessageQueueAveragely();
        List<MessageQueue> group = allocateMessageQueueAveragely.allocate("group", "5", messageQueueList, Arrays.asList("1", "2", "3", "4", "5"));
        for (MessageQueue queue : group) {
            System.out.println(queue);
        }
    }

    @Override
    public String getName() {
        return "AVG";
    }
}