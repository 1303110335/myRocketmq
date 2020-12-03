/**
 * bianque.com
 * Copyright (C) 2013-2020 All Rights Reserved.
 */
package com.rocketmq.store.config;

import com.rocketmq.store.ConsumeQueue;

import java.io.File;

/**
 * @author xuleyan
 * @version BrokerConfig.java, v 0.1 2020-10-29 8:28 下午
 */
public class MessageStoreConfig {

    private String storePathRootDir = System.getProperty("user.home") + File.separator + "mystore";

    private String storePathCommitLog = System.getProperty("user.home") + File.separator + "mystore" +
            File.separator + "commitlog";

    private int haListenPort = 10912;

    /**
     * 每个commitLog文件大小
     */
    private int mapedFileSizeCommitLog = 1024 * 1024 * 1024;

    /**
     * 每个消费队列文件大小，每个文件30W消息
     */
    private int mapedFileSizeConsumeQueue = 300000 * ConsumeQueue.CQ_STORE_UNIT_SIZE;

    /**
     * flush commitLog 周期，单位：毫秒
     */
    private int flushIntervalCommitLog = 500;

    private BrokerRole brokerRole = BrokerRole.ASYNC_MASTER;

    public BrokerRole getBrokerRole() {
        return brokerRole;
    }

    public void setBrokerRole(BrokerRole brokerRole) {
        this.brokerRole = brokerRole;
    }

    public String getStorePathRootDir() {
        return storePathRootDir;
    }

    public void setStorePathRootDir(String storePathRootDir) {
        this.storePathRootDir = storePathRootDir;
    }

    public String getStorePathCommitLog() {
        return storePathCommitLog;
    }

    public void setStorePathCommitLog(String storePathCommitLog) {
        this.storePathCommitLog = storePathCommitLog;
    }

    public int getMapedFileSizeCommitLog() {
        return mapedFileSizeCommitLog;
    }

    public void setMapedFileSizeCommitLog(int mapedFileSizeCommitLog) {
        this.mapedFileSizeCommitLog = mapedFileSizeCommitLog;
    }

    public int getMapedFileSizeConsumeQueue() {
        return mapedFileSizeConsumeQueue;
    }

    public void setMapedFileSizeConsumeQueue(int mapedFileSizeConsumeQueue) {
        this.mapedFileSizeConsumeQueue = mapedFileSizeConsumeQueue;
    }

    public int getFlushIntervalCommitLog() {
        return flushIntervalCommitLog;
    }

    public void setFlushIntervalCommitLog(int flushIntervalCommitLog) {
        this.flushIntervalCommitLog = flushIntervalCommitLog;
    }

    public int getHaListenPort() {
        return haListenPort;
    }

    public void setHaListenPort(int haListenPort) {
        this.haListenPort = haListenPort;
    }
}