/**
 * bianque.com
 * Copyright (C) 2013-2020All Rights Reserved.
 */
package com.rocketmq.remoting.netty;

import com.rocketmq.remoting.exception.RemotingCommandException;
import com.rocketmq.remoting.protocol.RemotingCommand;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author xuleyan
 * @version NettyRequestProcessor.java, v 0.1 2020-10-10 11:33 上午
 */
public interface NettyRequestProcessor {
    RemotingCommand processCommand(ChannelHandlerContext ctx, RemotingCommand request) throws RemotingCommandException;

    boolean rejectRequest();
}