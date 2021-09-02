/**
 * bianque.com
 * Copyright (C) 2013-2020 All Rights Reserved.
 */
package com.rocketmq.namesrv.processor;

import com.rocketmq.common.namesrv.RegisterBrokerResult;
import com.rocketmq.common.protocol.RequestCode;
import com.rocketmq.common.protocol.body.RegisterBrokerBody;
import com.rocketmq.common.protocol.header.namesrv.RegisterBrokerRequestHeader;
import com.rocketmq.common.protocol.header.namesrv.RegisterBrokerResponseHeader;
import com.rocketmq.namesrv.NamesrvController;
import com.rocketmq.remoting.common.RemotingHelper;
import com.rocketmq.remoting.exception.RemotingCommandException;
import com.rocketmq.remoting.netty.NettyRequestProcessor;
import com.rocketmq.remoting.protocol.RemotingCommand;
import com.rocketmq.remoting.protocol.ResponseCode;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author xuleyan
 * @version DefaultRequestProcessor.java, v 0.1 2020-10-10 2:05 下午
 */
@Slf4j
public class DefaultRequestProcessor implements NettyRequestProcessor {

    protected final NamesrvController namesrvController;

    public DefaultRequestProcessor(NamesrvController namesrvController) {
        this.namesrvController = namesrvController;
    }

    @Override
    public RemotingCommand processCommand(ChannelHandlerContext ctx, RemotingCommand request) throws RemotingCommandException {
        log.debug("DefaultRequestProcessor >> receive request, ctx = {}, remoteAddr = {}, request = {}",
                ctx, RemotingHelper.parseChannelRemoteAddr(ctx.channel()), request);

        switch (request.getCode()) {
            case RequestCode.REGISTER_BROKER:
                return this.registerBrokerWithFilterServer(ctx, request);
            default:
                break;
        }
        return null;
    }

    private RemotingCommand registerBrokerWithFilterServer(ChannelHandlerContext ctx, RemotingCommand request) throws RemotingCommandException {
        log.info("{} >> registerBrokerWithFilterServer >> request = {}", Thread.currentThread().getName(), request);
        final RemotingCommand response = RemotingCommand.createResponseCommand(RegisterBrokerResponseHeader.class);
        final RegisterBrokerResponseHeader responseHeader = (RegisterBrokerResponseHeader) response.readCustomHeader();
        RegisterBrokerRequestHeader requestHeader = (RegisterBrokerRequestHeader) request.decodeCommandCustomHeader(RegisterBrokerRequestHeader.class);

        RegisterBrokerBody registerBrokerBody = new RegisterBrokerBody();
        if (request.getBody() != null) {
            registerBrokerBody = RegisterBrokerBody.decode(request.getBody(), RegisterBrokerBody.class);
        } else {
            registerBrokerBody.getTopicConfigSerializeWrapper().getDataVersion().setCounter(new AtomicLong(0));
            registerBrokerBody.getTopicConfigSerializeWrapper().getDataVersion().setTimestamp(0);
        }

        RegisterBrokerResult result = this.namesrvController.getRouteInfoManager().registerBroker(
                requestHeader.getClusterName(),
                requestHeader.getBrokerAddr(),
                requestHeader.getBrokerName(),
                requestHeader.getBrokerId(),
                requestHeader.getHaServerAddr(),
                registerBrokerBody.getTopicConfigSerializeWrapper(),
                registerBrokerBody.getFilterServerList(),
                ctx.channel()
        );
        responseHeader.setHaServerAddr(result.getHaServerAddr());
        responseHeader.setMasterAddr(result.getMasterAddr());

        // kvConfigManager
        response.setCode(ResponseCode.SUCCESS);
        response.setRemark(null);
        return response;
    }

    @Override
    public boolean rejectRequest() {
        return false;
    }
}