/**
 * bianque.com
 * Copyright (C) 2013-2020 All Rights Reserved.
 */
package com.rocketmq.namesrv.routeinfo;

import com.rocketmq.remoting.ChannelEventListener;

import java.nio.channels.Channel;

/**
 * @author xuleyan
 * @version BrokerHousekeepingService.java, v 0.1 2020-10-14 8:20 上午
 */
public class BrokerHousekeepingService implements ChannelEventListener {

    @Override
    public void onChannelConnect(String remoteAddr, io.netty.channel.Channel channel) {

    }

    @Override
    public void onChannelClose(String remoteAddr, io.netty.channel.Channel channel) {

    }

    @Override
    public void onChannelException(String remoteAddr, io.netty.channel.Channel channel) {

    }

    @Override
    public void onChannelIdle(String remoteAddr, io.netty.channel.Channel channel) {

    }
}