/**
 * bianque.com
 * Copyright (C) 2013-2020 All Rights Reserved.
 */
package com.rocketmq.broker.offset;

import com.rocketmq.broker.BrokerController;
import com.rocketmq.broker.BrokerPathConfigHelper;
import com.rocketmq.common.ConfigManager;
import com.rocketmq.remoting.protocol.RemotingSerializable;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xuleyan
 * @version ConsumerOffsetManager.java, v 0.1 2020-10-29 10:38 下午
 */
@Slf4j
public class ConsumerOffsetManager extends ConfigManager {

    private static final String TOPIC_GROUP_SEPARATOR = "@";

    /**
     * 消费进度集合
     */
    private ConcurrentHashMap<String/*topic@group*/, ConcurrentHashMap<Integer, Long>> offsetTable = new ConcurrentHashMap<>(512);

    private transient BrokerController brokerController;

    public ConsumerOffsetManager() {
    }

    public ConsumerOffsetManager(BrokerController brokerController) {
        this.brokerController = brokerController;
    }

    /**
     * 提交消费进度
     *
     * @param clientHost 提交client地址
     * @param group      消费分组
     * @param topic      主题
     * @param queueId    队列编号
     * @param offset     进度（队列位置）
     */
    public void commitOffset(final String clientHost, final String group, final String topic, final int queueId, final long offset) {
        // topic@group
        String key = topic + TOPIC_GROUP_SEPARATOR + group;
        this.commitOffset(clientHost, key, queueId, offset);
    }

    /**
     * 提交消费进度
     *
     * @param clientHost 提交client地址
     * @param key        主题@消费分组,topic@consumeGroup
     * @param queueId    队列编号
     * @param offset     进度
     */
    private void commitOffset(final String clientHost, final String key, final int queueId, final long offset) {
        ConcurrentHashMap<Integer, Long> map = this.offsetTable.get(key);
        if (null == map) {
            map = new ConcurrentHashMap<>(32);
            map.put(queueId, offset);
            this.offsetTable.put(key, map);
        } else {
            Long storeOffset = map.put(queueId, offset);
            if (storeOffset != null && offset < storeOffset) {
                log.warn("[NOTIFYME]update consumer offset less than store. clientHost={}, key={}, queueId={}, requestOffset={}, storeOffset={}",
                        clientHost, key, queueId, offset, storeOffset);
            }
        }
    }

    public ConcurrentHashMap<String, ConcurrentHashMap<Integer, Long>> getOffsetTable() {
        return offsetTable;
    }

    public void setOffsetTable(ConcurrentHashMap<String, ConcurrentHashMap<Integer, Long>> offsetTable) {
        this.offsetTable = offsetTable;
    }

    /**
     * 查询下标
     *
     * @param group
     * @param topic
     * @return
     */
    public Map<Integer, Long> queryOffset(final String group, final String topic) {
        String key = topic + TOPIC_GROUP_SEPARATOR + group;
        return this.offsetTable.get(key);
    }

    @Override
    public String encode() {
        return this.encode(false);
    }

    @Override
    public void decode(String jsonString) {
        if (jsonString != null) {
            ConsumerOffsetManager consumerOffsetManager = RemotingSerializable.fromJson(jsonString, ConsumerOffsetManager.class);
            if (consumerOffsetManager != null) {
                this.offsetTable = consumerOffsetManager.offsetTable;
            }
        }
    }

    @Override
    public String configFilePath() {
        return BrokerPathConfigHelper.getConsumerOffsetPath(this.brokerController.getMessageStoreConfig().getStorePathRootDir());
    }

    @Override
    public String encode(boolean prettyFormat) {
        return RemotingSerializable.toJson(this, prettyFormat);
    }
}