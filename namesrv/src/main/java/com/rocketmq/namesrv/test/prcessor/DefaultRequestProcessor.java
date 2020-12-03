/**
 * bianque.com
 * Copyright (C) 2013-2020 All Rights Reserved.
 */
package com.rocketmq.namesrv.test.prcessor;

import com.rocketmq.common.protocol.RequestCode;
import com.rocketmq.namesrv.test.NamesrvController;
import com.rocketmq.remoting.common.RemotingHelper;
import com.rocketmq.remoting.exception.RemotingCommandException;
import com.rocketmq.remoting.netty.NettyRequestProcessor;
import com.rocketmq.remoting.protocol.RemotingCommand;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.List;

/**
 *
 * @author xuleyan
 * @version DefaultRequestProcessor.java, v 0.1 2020-12-03 3:32 下午
 */
@Slf4j
public class DefaultRequestProcessor implements NettyRequestProcessor {
    protected final NamesrvController namesrvController;

    public DefaultRequestProcessor(NamesrvController namesrvController) {
        this.namesrvController = namesrvController;
    }

    private List<String> brokerList;


    @Override
    public RemotingCommand processCommand(ChannelHandlerContext ctx, RemotingCommand request) throws RemotingCommandException {
        log.debug("receive request, ctx = {}, remoteAddr = {}, request = {}",
                ctx, RemotingHelper.parseChannelRemoteAddr(ctx.channel()), request);

        switch (request.getCode()) {
            case RequestCode.REGISTER_BROKER:
                return this.registerBroker(ctx, request);
            default:
                break;
        }
        return null;
    }

    private RemotingCommand registerBroker(ChannelHandlerContext ctx, RemotingCommand request) {
        SocketAddress address = ctx.channel().remoteAddress();
        String s = address.toString();
        brokerList.add(s.substring(1));
        return request;
    }

    @Override
    public boolean rejectRequest() {
        return false;
    }
}