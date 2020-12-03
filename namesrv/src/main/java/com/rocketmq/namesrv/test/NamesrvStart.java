/**
 * bianque.com
 * Copyright (C) 2013-2020 All Rights Reserved.
 */
package com.rocketmq.namesrv.test;

import com.rocketmq.remoting.test.BrokerInfo;
import com.rocketmq.remoting.test.NettyRemoteServer;

/**
 *
 * @author xuleyan
 * @version NamesrvStart.java, v 0.1 2020-12-03 3:30 下午
 */
public class NamesrvStart {

    public static void main(String[] args) {

        NamesrvController namesrvController = new NamesrvController();
        namesrvController.init();
        namesrvController.start();
    }
}