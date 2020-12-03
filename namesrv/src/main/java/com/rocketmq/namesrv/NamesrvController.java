/**
 * bianque.com
 * Copyright (C) 2013-2020 All Rights Reserved.
 */
package com.rocketmq.namesrv;

import com.rocketmq.common.namesrv.NamesrvConfig;
import com.rocketmq.namesrv.routeinfo.BrokerHousekeepingService;
import com.rocketmq.namesrv.routeinfo.RouteInfoManager;
import com.rocketmq.remoting.RemotingServer;
import com.rocketmq.remoting.RemotingService;
import com.rocketmq.remoting.netty.NettyRemotingServer;
import com.rocketmq.remoting.netty.NettyServerConfig;

/**
 * @author xuleyan
 * @version NamesrvController.java, v 0.1 2020-10-12 10:21 上午
 */
public class NamesrvController {

    private final RouteInfoManager routeInfoManager;

    private RemotingService remotingServer;

    private final NamesrvConfig namesrvConfig;

    private final NettyServerConfig nettyServerConfig;

    private BrokerHousekeepingService brokerHousekeepingService;

    public NamesrvController(NamesrvConfig namesrvConfig, NettyServerConfig nettyServerConfig) {
        this.namesrvConfig = namesrvConfig;
        this.nettyServerConfig = nettyServerConfig;
        routeInfoManager = new RouteInfoManager();
    }

    public RouteInfoManager getRouteInfoManager() {
        return routeInfoManager;
    }

    public boolean initialize() {
        this.remotingServer = new NettyRemotingServer(this.nettyServerConfig, this.brokerHousekeepingService);

        return true;
    }

    public void start() {
        this.remotingServer.start();
    }

    public void shutdown() {
        this.remotingServer.shutdown();
    }
}