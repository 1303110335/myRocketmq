/**
 * bianque.com
 * Copyright (C) 2013-2020 All Rights Reserved.
 */
package com.rocketmq.namesrv.routeinfo;

import com.rocketmq.common.DataVersion;
import com.rocketmq.common.MixAll;
import com.rocketmq.common.namesrv.RegisterBrokerResult;
import com.rocketmq.common.protocol.TopicConfig;
import com.rocketmq.common.protocol.body.TopicConfigSerializeWrapper;
import com.rocketmq.common.protocol.route.BrokerData;
import com.rocketmq.common.protocol.route.QueueData;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author xuleyan
 * @version RouteInfoManager.java, v 0.1 2020-10-11 4:00 下午
 */
@Slf4j
public class RouteInfoManager {

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * 集群 与 broker集合 Map
     */
    private final HashMap<String/* clusterName */, Set<String/* brokerName */>> clusterAddrTable;

    /**
     * topic 与 队列数据数组 Map
     * 一个 topic 可以对应 多个Broker
     */
    private final HashMap<String/*topic*/, List<QueueData>> topicQueueTable;

    /**
     * broker名 与 broker数据 Map
     * 一个broker名下可以有多台机器
     */
    private final HashMap<String/*brokerName*/, BrokerData> brokerAddrTable;

    /**
     * broker地址 与 broker连接信息 Map
     */
    private final HashMap<String/*brokerAddr*/, BrokerLiveInfo> brokerLiveTable;

    /**
     * broker地址 与 filtersrv数组 Map
     */
    private final HashMap<String/* brokerAddr */, List<String>/* Filter Server */> filterServerTable;


    public RouteInfoManager() {
        this.clusterAddrTable = new HashMap<>(256);
        this.topicQueueTable = new HashMap<>(1024);
        this.brokerAddrTable = new HashMap<>(138);
        this.brokerLiveTable = new HashMap<>(256);
        this.filterServerTable = new HashMap<>(256);
    }

    public RegisterBrokerResult registerBroker(
            final String clusterName,
            final String brokerAddr,
            final String brokerName,
            final long brokerId,
            final String haServerAddr,
            final TopicConfigSerializeWrapper topicConfigWrapper,
            final List<String> filterServerList,
            final Channel channel
    ) {
        RegisterBrokerResult result = new RegisterBrokerResult();
        try {
            this.lock.writeLock().lockInterruptibly();

            // 更新集群信息
            Set<String> brokerNames = this.clusterAddrTable.get(clusterName);
            if (null == brokerNames) {
                brokerNames = new HashSet<>();
                this.clusterAddrTable.put(brokerName, brokerNames);
            }
            brokerNames.add(brokerName);

            boolean registerFirst = false;
            BrokerData brokerData = this.brokerAddrTable.get(brokerName);
            if (brokerData == null) {
                registerFirst = true;
                brokerData = new BrokerData();
                brokerData.setBrokerName(brokerName);
                HashMap<Long, String> brokerAddrs = new HashMap<>();
                brokerData.setBrokerAddrs(brokerAddrs);
                this.brokerAddrTable.put(brokerName, brokerData);
            }
            String oldAddr = brokerData.getBrokerAddrs().put(brokerId, brokerAddr);
            log.info("brokerId = {}, brokerAddr = {} 注册成功", brokerId, brokerAddr);
            registerFirst = registerFirst || (oldAddr == null);
            // 有配置且是master
            if (null != topicConfigWrapper && MixAll.MASTER_ID == brokerId) {
                if (this.isBrokerTopicConfigChanged(brokerAddr, topicConfigWrapper.getDataVersion()) || registerFirst) {
                    ConcurrentHashMap<String, TopicConfig> topicConfigTable = topicConfigWrapper.getTopicConfigTable();
                    if (topicConfigTable != null) {
                        for (Map.Entry<String, TopicConfig> entry : topicConfigTable.entrySet()) {
                            // master节点更新topic队列信息
                            this.createAndUpdateQueueData(brokerName, entry.getValue());
                        }
                    }
                }
            }

            // 更新broker连接信息
            BrokerLiveInfo preBrokerLiveInfo = this.brokerLiveTable.put(brokerAddr,
                    new BrokerLiveInfo(
                            System.currentTimeMillis(),
                            topicConfigWrapper.getDataVersion(),
                            channel,
                            haServerAddr
                    ));
            if (preBrokerLiveInfo == null) {
                log.info("new broker registerd, {} HAServer: {}", brokerAddr, haServerAddr);
            }

            // 更新filtersrv信息
            if (filterServerList != null) {
                if (filterServerList.isEmpty()) {
                    this.filterServerTable.remove(brokerAddr);
                } else {
                    this.filterServerTable.put(brokerAddr, filterServerList);
                }
            }

            // 当注册Slave时,返回Master的addr作为高可用的地址
            if (MixAll.MASTER_ID != brokerId) {
                String masterAddr = brokerData.getBrokerAddrs().get(MixAll.MASTER_ID);
                if (masterAddr != null) {
                    BrokerLiveInfo brokerLiveInfo = this.brokerLiveTable.get(masterAddr);
                    if (brokerLiveInfo != null) {
                        result.setHaServerAddr(brokerLiveInfo.getHaServerAddr());
                        result.setMasterAddr(masterAddr);
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("register brokerException ", e);
        } finally {
            this.lock.writeLock().unlock();
        }
        return result;
    }

    private void createAndUpdateQueueData(final String brokerName, final TopicConfig config) {
        QueueData queueData = new QueueData();
        queueData.setBrokerName(brokerName);
        queueData.setWriteQueueNums(config.getWriteQueueNums());
        queueData.setReadQueueNums(config.getReadQueueNums());
        queueData.setPerm(config.getPerm());
        queueData.setTopicSynFlag(config.getTopicSysFlag());

        String topicName = config.getTopicName();
        List<QueueData> queueDataList = this.topicQueueTable.get(topicName);
        if (queueDataList == null) {
            queueDataList = new LinkedList<>();
            queueDataList.add(queueData);
            this.topicQueueTable.put(topicName, queueDataList);
            log.info(" new topic registered : topicName = {}, queueData = {}", topicName, queueData);
        } else {
            boolean addNewOne = true;
            Iterator<QueueData> iterator = queueDataList.iterator();
            while (iterator.hasNext()) {
                QueueData data = iterator.next();
                if (data.getBrokerName().equals(brokerName)) {
                    if (data.equals(queueData)) {
                        addNewOne = false;
                    } else {
                        log.info("queue data changed : topicName = {}, old = {},  new = {}", topicName, data, queueData);
                        iterator.remove();
                    }
                }
            }
            if (addNewOne) {
                queueDataList.add(queueData);
            }
        }

    }

    private boolean isBrokerTopicConfigChanged(final String brokerAddr, final DataVersion dataVersion) {
        BrokerLiveInfo prev = this.brokerLiveTable.get(brokerAddr);
        if (prev == null || !prev.getDataVersion().equals(dataVersion)) {
            return true;
        }
        return false;
    }

    public void printAllPeriodically() {
        try {
            this.lock.readLock().lockInterruptibly();
            log.info("--------------------------------------------------------");
            {
                log.info("topicQueueTable SIZE: {}", this.topicQueueTable.size());
                Iterator<Map.Entry<String, List<QueueData>>> iterator = this.topicQueueTable.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, List<QueueData>> next = iterator.next();
                    log.info("topicQueueTable Topic: {} {}", next.getKey(), next.getValue());
                }
            }

            {
                log.info("brokerAddrTable SIZE: {}", this.brokerAddrTable.size());
                Iterator<Map.Entry<String, BrokerData>> it = this.brokerAddrTable.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, BrokerData> next = it.next();
                    log.info("brokerAddrTable brokerName: {} {}", next.getKey(), next.getValue());
                }
            }

            {
                log.info("brokerLiveTable SIZE: {}", this.brokerLiveTable.size());
                Iterator<Map.Entry<String, BrokerLiveInfo>> it = this.brokerLiveTable.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, BrokerLiveInfo> next = it.next();
                    log.info("brokerLiveTable brokerAddr: {} {}", next.getKey(), next.getValue());
                }
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            this.lock.readLock().unlock();
        }
    }
}

class BrokerLiveInfo {
    /**
     * 最后更新时间
     */
    private long lastUpdateTimestamp;
    /**
     * 数据版本号
     */
    private DataVersion dataVersion;
    /**
     * 连接信息
     */
    private Channel channel;
    /**
     * ha服务器地址
     */
    private String haServerAddr;

    public BrokerLiveInfo(long lastUpdateTimestamp, DataVersion dataVersion, Channel channel,
                          String haServerAddr) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
        this.dataVersion = dataVersion;
        this.channel = channel;
        this.haServerAddr = haServerAddr;
    }

    public long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    public void setLastUpdateTimestamp(long lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    public DataVersion getDataVersion() {
        return dataVersion;
    }

    public void setDataVersion(DataVersion dataVersion) {
        this.dataVersion = dataVersion;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String getHaServerAddr() {
        return haServerAddr;
    }

    public void setHaServerAddr(String haServerAddr) {
        this.haServerAddr = haServerAddr;
    }

    @Override
    public String toString() {
        return "BrokerLiveInfo [lastUpdateTimestamp=" + lastUpdateTimestamp + ", dataVersion=" + dataVersion
                + ", channel=" + channel + ", haServerAddr=" + haServerAddr + "]";
    }
}