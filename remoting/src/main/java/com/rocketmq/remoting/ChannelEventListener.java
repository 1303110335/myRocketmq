/**
 * bianque.com
 * Copyright (C) 2013-2020 All Rights Reserved.
 */
package com.rocketmq.remoting;


import io.netty.channel.Channel;

/**
 * 连接时间监听器,用于心跳的事件监听
 *
 * @author xuleyan
 * @version ChannelEventListener.java, v 0.1 2020-10-14 8:21 上午
 */
public interface ChannelEventListener {

    /**
     * 收到心跳连接
     *
     * @param remoteAddr
     * @param channel
     */
    void onChannelConnect(final String remoteAddr, final Channel channel);

    /**
     * 心跳连接断开
     *
     * @param remoteAddr
     * @param channel
     */
    void onChannelClose(final String remoteAddr, final io.netty.channel.Channel channel);

    /**
     * 心跳连接出现异常
     *
     * @param remoteAddr
     * @param channel
     */
    void onChannelException(final String remoteAddr, final io.netty.channel.Channel channel);

    /**
     * 心跳连接空闲
     *
     * @param remoteAddr
     * @param channel
     */
    void onChannelIdle(final String remoteAddr, final io.netty.channel.Channel channel);
}