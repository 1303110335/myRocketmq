/**
 * bianque.com
 * Copyright (C) 2013-2020 All Rights Reserved.
 */
package com.rocketmq.namesrv;

import com.rocketmq.common.namesrv.NamesrvConfig;
import com.rocketmq.remoting.netty.NettyServerConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * @author xuleyan
 * @version NamesrvStartup.java, v 0.1 2020-10-19 10:56 下午
 */
@Slf4j
public class NamesrvStartup {

    public static void main(String[] args) {
        main0(args);
    }

    private static NamesrvController main0(String[] args) {
        final NamesrvConfig namesrvConfig = new NamesrvConfig();

        final NettyServerConfig nettyServerConfig = new NettyServerConfig();
        nettyServerConfig.setListenPort(9876);

        String dir = System.getProperty("user.dir");
        namesrvConfig.setRocketMqHome(dir);
        final NamesrvController controller = new NamesrvController(namesrvConfig, nettyServerConfig);
        boolean initialResult = controller.initialize();
        if (!initialResult) {
            controller.shutdown();
            System.exit(-3);
        }

        controller.start();
        log.info("myRocketmq start success");
        return controller;

    }
}