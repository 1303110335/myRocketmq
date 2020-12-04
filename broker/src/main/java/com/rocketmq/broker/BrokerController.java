/**
 * bianque.com
 * Copyright (C) 2013-2020 All Rights Reserved.
 */
package com.rocketmq.broker;

import com.rocketmq.broker.filtersrv.FilterServerManager;
import com.rocketmq.broker.offset.ConsumerOffsetManager;
import com.rocketmq.broker.out.BrokerOuterAPI;
import com.rocketmq.broker.topic.TopicConfigManager;
import com.rocketmq.common.BrokerConfig;
import com.rocketmq.common.Configuration;
import com.rocketmq.common.ThreadFactoryImpl;
import com.rocketmq.common.constant.PermName;
import com.rocketmq.common.namesrv.RegisterBrokerResult;
import com.rocketmq.common.protocol.TopicConfig;
import com.rocketmq.common.protocol.body.TopicConfigSerializeWrapper;
import com.rocketmq.remoting.RemotingServer;
import com.rocketmq.remoting.common.RemotingUtil;
import com.rocketmq.remoting.netty.NettyClientConfig;
import com.rocketmq.remoting.netty.NettyRemotingServer;
import com.rocketmq.remoting.netty.NettyServerConfig;
import com.rocketmq.store.config.MessageStoreConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author xuleyan
 * @version BrokerController.java, v 0.1 2020-10-29 9:15 下午
 */
@Slf4j
public class BrokerController {

    private final BrokerConfig brokerConfig;
    private final NettyServerConfig nettyServerConfig;
    private final NettyClientConfig nettyClientConfig;
    private final MessageStoreConfig messageStoreConfig;

    private final ConsumerOffsetManager consumerOffsetManager;
    private Configuration configuration;
    private TopicConfigManager topicConfigManager;
    private final BrokerOuterAPI brokerOuterAPI;

    private NettyRemotingServer remotingServer;

    private final FilterServerManager filterServerManager;

    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryImpl(
            "BrokerControllerScheduledThread"));


    public BrokerController(BrokerConfig brokerConfig, NettyServerConfig nettyServerConfig, NettyClientConfig nettyClientConfig, MessageStoreConfig messageStoreConfig) {
        this.brokerConfig = brokerConfig;
        this.nettyServerConfig = nettyServerConfig;
        this.nettyClientConfig = nettyClientConfig;
        this.messageStoreConfig = messageStoreConfig;
        this.consumerOffsetManager = new ConsumerOffsetManager(this);
        this.topicConfigManager = new TopicConfigManager(this);
        this.brokerOuterAPI = new BrokerOuterAPI(nettyClientConfig);
        this.filterServerManager = new FilterServerManager(this);
        this.configuration = new Configuration();
    }


    public boolean initialize() {
        boolean result = true;

        result = result && this.topicConfigManager.load();

        this.remotingServer = new NettyRemotingServer(this.nettyServerConfig, null);


        return true;
    }

    public void shutdown() {

    }

    public BrokerConfig getBrokerConfig() {
        return brokerConfig;
    }

    public NettyServerConfig getNettyServerConfig() {
        return nettyServerConfig;
    }

    public NettyClientConfig getNettyClientConfig() {
        return nettyClientConfig;
    }

    public MessageStoreConfig getMessageStoreConfig() {
        return messageStoreConfig;
    }

    public ConsumerOffsetManager getConsumerOffsetManager() {
        return consumerOffsetManager;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public TopicConfigManager getTopicConfigManager() {
        return topicConfigManager;
    }

    public void setTopicConfigManager(TopicConfigManager topicConfigManager) {
        this.topicConfigManager = topicConfigManager;
    }


    public String getBrokerAddr() {
        return this.brokerConfig.getBrokerIP1() + ":" + this.nettyServerConfig.getListenPort();
    }

    public String getHAServerAddr() {
        return this.brokerConfig.getBrokerIP2() + ":" + this.messageStoreConfig.getHaListenPort();
    }

    /**
     * 向Namesrc注册Broker信息,每隔30S执行一次
     *
     * @param checkOrderConfig
     * @param oneway
     */
    public synchronized void registerBrokerAll(final boolean checkOrderConfig, boolean oneway) {
        TopicConfigSerializeWrapper topicConfigWrapper = this.getTopicConfigManager().buildTopicConfigSerializeWrapper();
        if (!PermName.isWriteable(this.brokerConfig.getBrokerPermission())
                || !PermName.isReadable(this.brokerConfig.getBrokerPermission())) {
            ConcurrentHashMap<String, TopicConfig> topicConfigTable = new ConcurrentHashMap<String, TopicConfig>();
            for (TopicConfig topicConfig : topicConfigWrapper.getTopicConfigTable().values()) {
                TopicConfig tmp =
                        new TopicConfig(topicConfig.getTopicName(), topicConfig.getReadQueueNums(), topicConfig.getWriteQueueNums(),
                                this.brokerConfig.getBrokerPermission());
                topicConfigTable.put(topicConfig.getTopicName(), tmp);
            }
            topicConfigWrapper.setTopicConfigTable(topicConfigTable);
        }

        RegisterBrokerResult registerBrokerResult = this.brokerOuterAPI.registerBrokerAll(
                this.brokerConfig.getBrokerClusterName(),
                this.getBrokerAddr(),
                this.brokerConfig.getBrokerName(),
                this.brokerConfig.getBrokerId(),
                this.getHAServerAddr(),
                topicConfigWrapper,
                this.filterServerManager.buildNewFilterServerList(),
                oneway,
                this.brokerConfig.getRegisterBrokerTimeoutMills());
        log.info("registerBrokerResult = {}", registerBrokerResult);
    }

    public void start() {
        // 一堆初始化...

        if (this.remotingServer != null) {
            this.remotingServer.start();
        }

        this.registerBrokerAll(true, false);

//        this.scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
//
//            @Override
//            public void run() {
//                try {
//                    BrokerController.this.registerBrokerAll(true, false);
//                } catch (Throwable e) {
//                    log.error("registerBrokerAll Exception", e);
//                }
//            }
//        }, 1000 * 10, 1000 * 30, TimeUnit.MILLISECONDS);
    }
}