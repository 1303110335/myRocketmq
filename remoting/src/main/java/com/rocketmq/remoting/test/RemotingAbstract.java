/**
 * bianque.com
 * Copyright (C) 2013-2020 All Rights Reserved.
 */
package com.rocketmq.remoting.test;

import com.rocketmq.remoting.RemotingServer;
import com.rocketmq.remoting.common.Pair;
import com.rocketmq.remoting.common.RemotingHelper;
import com.rocketmq.remoting.exception.RemotingSendRequestException;
import com.rocketmq.remoting.exception.RemotingTimeoutException;
import com.rocketmq.remoting.netty.NettyRequestProcessor;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * @author xuleyan
 * @version RemotingAbstract.java, v 0.1 2020-11-29 7:58 下午
 */
@Slf4j
public abstract class RemotingAbstract implements RemotingServer {


    protected final ConcurrentHashMap<Integer/* opaque*/, ResponseFuture> responseTable = new ConcurrentHashMap<Integer, ResponseFuture>();

    protected Pair<NettyRequestProcessor, ExecutorService> defaultRequestProcessor;


    @Override
    public void registerDefaultProcessor(NettyRequestProcessor processor, ExecutorService executor) {
        this.defaultRequestProcessor = new Pair<>(processor, executor);
    }

    public void processMessageReceived(ChannelHandlerContext ctx, RemoteCommand msg) throws Exception {
        final RemoteCommand cmd = msg;
        log.info("接收到的信息 >> cmd = {}", cmd);
        if (cmd != null && cmd.getType() != null) {
            switch (cmd.getType()) {
                case REQUEST_COMMAND:
                    processRequestCommand(ctx, cmd);
                    break;
                case RESPONSE_COMMAND:
                    processResponseCommand(ctx, cmd);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 客户端处理返回数据
     *
     * @param ctx
     * @param cmd
     */
    private void processResponseCommand(ChannelHandlerContext ctx, RemoteCommand cmd) {
        ResponseFuture responseFuture = responseTable.get(cmd.getOpaque());
        if (responseFuture == null) {
            log.error("responseFuture not found, cmd = {}", cmd);
            return;
        }
        responseFuture.putResponse(cmd);
    }

    /**
     * 服务器端处理请求数据
     *
     * @param ctx
     * @param cmd
     */
    private void processRequestCommand(ChannelHandlerContext ctx, RemoteCommand cmd) {
        // defaultRequestProcessor.processRequest(ctx, cmd);
    }

    protected RemoteCommand invokeSyncImpl(final Channel channel, final RemoteCommand request, final Long timeoutMillis) throws InterruptedException, RemotingSendRequestException, RemotingTimeoutException {
        final int opaque = request.getOpaque();

        try {
            final ResponseFuture responseFuture = new ResponseFuture(opaque, timeoutMillis);
            this.responseTable.put(opaque, responseFuture);
            SocketAddress addr = channel.remoteAddress();

            channel.writeAndFlush(request).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        responseFuture.setSendRequestOK(true);
                        return;
                    } else {
                        responseFuture.setSendRequestOK(false);
                    }
                    responseTable.remove(opaque);
                    responseFuture.setCause(future.cause());
                    responseFuture.putResponse(null);
                    log.warn("send a request command to channel <" + addr + "> failed.");
                }
            });

            RemoteCommand responseCommand = responseFuture.waitResponse(timeoutMillis);
            if (null == responseCommand) {
                if (responseFuture.isSendRequestOK()) {
                    throw new RemotingTimeoutException(RemotingHelper.parseSocketAddressAddr(addr), timeoutMillis,
                            responseFuture.getCause());
                } else {
                    throw new RemotingSendRequestException(RemotingHelper.parseSocketAddressAddr(addr), responseFuture.getCause());
                }
            }
            return responseCommand;
        } finally {
            this.responseTable.remove(opaque);
        }
    }
}