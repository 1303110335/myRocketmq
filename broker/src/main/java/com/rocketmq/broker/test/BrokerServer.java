/**
 * bianque.com
 * Copyright (C) 2013-2020 All Rights Reserved.
 */
package com.rocketmq.broker.test;


import com.rocketmq.remoting.InvokeCallback;
import com.rocketmq.remoting.RPCHook;
import com.rocketmq.remoting.protocol.RemotingCommand;
import com.rocketmq.remoting.test.RemoteCode;
import com.rocketmq.remoting.test.RemoteCommand;
import com.rocketmq.remoting.test.RemotingAbstract;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.nio.channels.Channel;
import java.nio.charset.StandardCharsets;

/**
 * @author xuleyan
 * @version BrokerServer.java, v 0.1 2020-10-09 10:20 上午
 */
public class BrokerServer extends RemotingAbstract {

    public static void main(String[] args) {
        BrokerServer brokerServer = new BrokerServer();
        brokerServer.start();
    }

    @Override
    public void start() {
        EventLoopGroup loopGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(loopGroup)
                    .channel(NioSocketChannel.class)
                    // Nagle算法通过减少需要传输的数据包，来优化网络
                    // 启动TCP_NODELAY，就意味着禁用了Nagle算法，允许小包的发送
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new ObjectEncoder());
                            pipeline.addLast(new ObjectDecoder(Integer.MAX_VALUE,
                                    ClassResolvers.cacheDisabled(null)));
                            pipeline.addLast(new ClientHandler());
                        }
                    });

            ChannelFuture future = bootstrap.connect("127.0.0.1", 8888).sync();
            // 要传输的参数
            RemoteCommand remoteCommand = new RemoteCommand();
            remoteCommand.setBody("我是haha".getBytes(StandardCharsets.UTF_8));
            remoteCommand.setRequestType(RemoteCode.REGISTER.getCode());

            future.channel().writeAndFlush(remoteCommand).sync();
            future.channel().closeFuture().sync();

            System.out.println("brokerServer启动");

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //loopGroup.shutdownGracefully();
        }
    }

    @Override
    public void shutdown() {

    }

    @Override
    public void registerRPCHook(RPCHook rpcHook) {

    }

    @Override
    public RemotingCommand invokeSync(Channel channel, RemotingCommand request, long timeoutMillis) {
        return null;
    }

    @Override
    public void invokeAsync(io.netty.channel.Channel channel, RemotingCommand request, long timeoutMillis, InvokeCallback invokeCallback) {

    }

    @Override
    public void invokeOneway(io.netty.channel.Channel channel, RemotingCommand request, long timeoutMillis) {

    }

    public class ClientHandler extends SimpleChannelInboundHandler<RemoteCommand> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, RemoteCommand msg) throws Exception {
            // msg 是远程返回的结果
            processMessageReceived(ctx, msg);

        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }

}