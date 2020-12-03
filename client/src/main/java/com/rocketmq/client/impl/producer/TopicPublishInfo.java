/**
 * bianque.com
 * Copyright (C) 2013-2020 All Rights Reserved.
 */
package com.rocketmq.client.impl.producer;

import com.rocketmq.client.test.ThreadLocalIndex;
import com.rocketmq.common.message.MessageQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author xuleyan
 * @version TopicPublishInfo.java, v 0.1 2020-11-28 1:35 下午
 */
public class TopicPublishInfo {

    private volatile ThreadLocalIndex sendWhichQueue = new ThreadLocalIndex();

    private List<MessageQueue> messageQueueList = new ArrayList<>();

    public ThreadLocalIndex getSendWhichQueue() {
        return sendWhichQueue;
    }

    public void setSendWhichQueue(ThreadLocalIndex sendWhichQueue) {
        this.sendWhichQueue = sendWhichQueue;
    }

    public List<MessageQueue> getMessageQueueList() {
        return messageQueueList;
    }

    public void setMessageQueueList(List<MessageQueue> messageQueueList) {
        this.messageQueueList = messageQueueList;
    }

    public static void main(String[] args) {
        TopicPublishInfo topicPublishInfo = new TopicPublishInfo();
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
        topicPublishInfo.setMessageQueueList(messageQueueList);

        topicPublishInfo.sendWhichQueue();
    }

    private void sendWhichQueue() {
        ExecutorService executorService = Executors.newFixedThreadPool(10, new ThreadFactory() {
            private volatile AtomicInteger integer = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("testMessage" + integer.incrementAndGet());
                return thread;
            }
        });

        for (int i = 0; i < 10; i++) {
            executorService.execute(this::selectOneMessageQueue);
        }

        executorService.shutdown();
    }


    /**
     * 直接选择上次发送队列的下一位
     *
     * @return
     */
    public MessageQueue selectOneMessageQueue() {
        int index = this.sendWhichQueue.getAndIncrement();
        System.out.println(Thread.currentThread().getName() + ",index= " + index);
        int pos = Math.abs(index) % this.messageQueueList.size();
        if (pos < 0) {
            pos = 0;
        }
        MessageQueue messageQueue = this.messageQueueList.get(pos);
        System.out.println(Thread.currentThread().getName() + ", queueId = " + messageQueue.getQueueId());
        return messageQueue;
    }

}