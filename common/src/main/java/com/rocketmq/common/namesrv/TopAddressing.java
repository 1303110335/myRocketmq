/**
 * bianque.com
 * Copyright (C) 2013-2020 All Rights Reserved.
 */
package com.rocketmq.common.namesrv;

import com.rocketmq.common.MixAll;
import com.rocketmq.remoting.RemotingClient;
import lombok.extern.slf4j.Slf4j;

/**
 * @author xuleyan
 * @version TopAddressing.java, v 0.1 2020-11-01 9:13 下午
 */
@Slf4j
public class TopAddressing {
    private String wsAddr;

    public TopAddressing(String wsAddr) {
        this.wsAddr = wsAddr;
    }


}