package com.rocketmq.namesrv.routeinfo.service;

import com.rocketmq.common.namesrv.RegisterBrokerResult;
import com.rocketmq.common.protocol.TopicConfig;
import com.rocketmq.common.protocol.body.TopicConfigSerializeWrapper;
import com.rocketmq.namesrv.routeinfo.RouteInfoManager;
import io.netty.channel.Channel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class RouteInfoManagerTest {

    private static RouteInfoManager routeInfoManager;

    @Before
    public void setUp() {
        routeInfoManager = new RouteInfoManager();
        testRegisterBroker();
    }

    @After
    public void terminate() {
        routeInfoManager.printAllPeriodically();
//        routeInfoManager.unregisterBroker();
    }

    @Test
    public void testRegisterBroker() {
        TopicConfigSerializeWrapper topicConfigSerializeWrapper = new TopicConfigSerializeWrapper();
        ConcurrentHashMap<String, TopicConfig> topicConfigConcurrentHashMap = new ConcurrentHashMap<>();
        TopicConfig topicConfig = new TopicConfig();
        topicConfig.setWriteQueueNums(8);
        topicConfig.setTopicName("unit-test");
        topicConfig.setPerm(6);
        topicConfig.setReadQueueNums(8);
        topicConfig.setOrder(false);
        topicConfigConcurrentHashMap.put("unit-test", topicConfig);
        topicConfigSerializeWrapper.setTopicConfigTable(topicConfigConcurrentHashMap);
        Channel channel = mock(Channel.class);
        RegisterBrokerResult result = routeInfoManager.registerBroker("default-cluster", "127.0.0.1:10911", "default-broker",
                0, "127.0.0.1:1001", topicConfigSerializeWrapper, new ArrayList<>(), channel);
        assertThat(result).isNotNull();
    }

}