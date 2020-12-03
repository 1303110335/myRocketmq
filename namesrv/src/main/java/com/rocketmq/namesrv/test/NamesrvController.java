/**
 * bianque.com
 * Copyright (C) 2013-2020 All Rights Reserved.
 */
package com.rocketmq.namesrv.test;

import com.rocketmq.namesrv.test.prcessor.DefaultRequestProcessor;
import com.rocketmq.remoting.RemotingServer;
import com.rocketmq.remoting.test.BrokerInfo;

/**
 * @author xuleyan
 * @version NamesrvController.java, v 0.1 2020-11-29 12:19 下午
 */
public class NamesrvController {

    private BrokerInfo brokerInfo;

    private RemotingServer remotingServer;

    public RemotingServer getRemotingServer() {
        return remotingServer;
    }

    public void setRemotingServer(RemotingServer remotingServer) {
        this.remotingServer = remotingServer;
    }

    public BrokerInfo getBrokerInfo() {
        return brokerInfo;
    }

    public NamesrvController() {
    }

    public void init() {
        brokerInfo = new BrokerInfo();
        //remotingServer.registerDefaultProcessor(new DefaultRequestProcessor(this));
    }

    public void start() {

    }
}