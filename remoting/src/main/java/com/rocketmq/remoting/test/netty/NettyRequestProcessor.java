/**
 * bianque.com
 * Copyright (C) 2013-2020 All Rights Reserved.
 */
package com.rocketmq.remoting.test.netty;

import com.rocketmq.remoting.exception.RemotingCommandException;
import com.rocketmq.remoting.test.RemoteCommand;
import io.netty.channel.ChannelHandlerContext;

/**
 *
 * @author xuleyan
 * @version NettyRequestProcessor.java, v 0.1 2020-12-03 3:38 下午
 */
public interface NettyRequestProcessor {

    RemoteCommand processCommand(ChannelHandlerContext ctx, RemoteCommand request) throws RemotingCommandException;

}