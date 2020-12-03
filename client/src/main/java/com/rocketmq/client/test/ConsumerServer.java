/**
 * bianque.com
 * Copyright (C) 2013-2020 All Rights Reserved.
 */
package com.rocketmq.client.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rocketmq.remoting.exception.RemotingSendRequestException;
import com.rocketmq.remoting.exception.RemotingTimeoutException;
import com.rocketmq.remoting.protocol.RemotingCommandType;
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
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author xuleyan
 * @version ConsumerServer.java, v 0.1 2020-11-29 10:52 上午
 */
@Slf4j
public class ConsumerServer extends RemotingAbstract {

    public static void main(String[] args) {
        ConsumerServer consumerServer = new ConsumerServer();
        consumerServer.start();
    }

    private void start() {
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
                            pipeline.addLast(new ConsumerHandler());
                        }
                    });

            ChannelFuture future = bootstrap.connect("127.0.0.1", 8888).sync();
            // 要传输的参数
            RemoteCommand remoteCommand = new RemoteCommand();
            remoteCommand.setBody("consumer注册".getBytes(StandardCharsets.UTF_8));
            remoteCommand.setRequestType(RemoteCode.QUERY.getCode());
            remoteCommand.setType(RemotingCommandType.RESPONSE_COMMAND);
//            future.channel().writeAndFlush(remoteCommand).sync();
//            future.channel().closeFuture().sync();

            RemoteCommand response = this.invokeSyncImpl(future.channel(), remoteCommand, 10000L);
            log.info("response = {}", response);

            String brokerListJson = new String(response.getBody(), StandardCharsets.UTF_8);
            List<String> brokerList = JSONObject.parseArray(brokerListJson, String.class);
            System.out.println(brokerList);

            // 从broker获取

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (RemotingTimeoutException e) {
            e.printStackTrace();
        } catch (RemotingSendRequestException e) {
            e.printStackTrace();
        } finally {
            //loopGroup.shutdownGracefully();
        }
    }

    /**
     * @author xuleyan
     * @version ConsumerHandler.java, v 0.1 2020-11-29 11:06 上午
     */
    public class ConsumerHandler extends SimpleChannelInboundHandler<RemoteCommand> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, RemoteCommand msg) throws Exception {
            processMessageReceived(ctx, msg);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }
}