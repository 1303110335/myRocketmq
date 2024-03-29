/**
 * bianque.com
 * Copyright (C) 2013-2020 All Rights Reserved.
 */
package com.rocketmq.remoting.netty;

import com.rocketmq.remoting.ChannelEventListener;
import com.rocketmq.remoting.RPCHook;
import com.rocketmq.remoting.RemotingService;
import com.rocketmq.remoting.protocol.RemotingCommand;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author xuleyan
 * @version RemotingServer.java, v 0.1 2020-10-10 10:44 上午
 */
@Slf4j
public class NettyRemotingServer extends NettyRemotingAbstract implements RemotingService {

    private final ServerBootstrap bootstrap;
    private final NettyServerConfig nettyServerConfig;
    private final ChannelEventListener channelEventListener;

    private final EventLoopGroup parentGroup;
    private final EventLoopGroup childGroup;

    public NettyRemotingServer(final NettyServerConfig nettyServerConfig, final ChannelEventListener channelEventListener) {
        super(nettyServerConfig.getServerOnewaySemaphoreValue(), nettyServerConfig.getServerAsyncSemaphoreValue());
        this.nettyServerConfig = nettyServerConfig;
        this.channelEventListener = channelEventListener;

        bootstrap = new ServerBootstrap();
        parentGroup = new NioEventLoopGroup(1, new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("parentGroup_%d", this.threadIndex.incrementAndGet()));
            }
        });

        childGroup = new NioEventLoopGroup(nettyServerConfig.getServerSelectorThreads(), new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("childGroup_%d", this.threadIndex.incrementAndGet()));
            }
        });
    }

    @Override
    public void start() {
        try {
            bootstrap.group(parentGroup, childGroup)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(nettyServerConfig.getListenPort()))
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            // 获取channel中的Pipeline
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new ObjectEncoder());
                            pipeline.addLast(new ObjectDecoder(Integer.MAX_VALUE,
                                    ClassResolvers.cacheDisabled(null)));
                            pipeline.addLast(new NettyServerHandler());
                        }
                    });
            ChannelFuture future = bootstrap.bind().sync();
            log.info("服务器已启动 >> port = {}, remoteAddress = {}", nettyServerConfig.getListenPort(), future.channel().remoteAddress());
            //future.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void shutdown() {
        if (parentGroup != null) {
            parentGroup.shutdownGracefully();
        }
        if (childGroup != null) {
            childGroup.shutdownGracefully();
        }
    }

    @Override
    public void registerRPCHook(RPCHook rpcHook) {

    }

    @Override
    public ChannelEventListener getChannelEventListener() {
        return null;
    }

    @Override
    public RPCHook getRPCHook() {
        return null;
    }

    @Override
    public ExecutorService getCallbackExecutor() {
        return null;
    }

    class NettyServerHandler extends SimpleChannelInboundHandler<RemotingCommand> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, RemotingCommand msg) throws Exception {
            processMessageReceived(ctx, msg);
        }
    }
}