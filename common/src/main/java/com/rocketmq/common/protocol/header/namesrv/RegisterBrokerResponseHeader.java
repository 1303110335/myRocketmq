/**
 * bianque.com
 * Copyright (C) 2013-2020 All Rights Reserved.
 */
package com.rocketmq.common.protocol.header.namesrv;

import com.rocketmq.remoting.CommandCustomHeader;
import com.rocketmq.remoting.annotation.CFNullable;

/**
 * @author xuleyan
 * @version RegisterBrokerResponseHeader.java, v 0.1 2020-10-10 3:07 下午
 */
public class RegisterBrokerResponseHeader implements CommandCustomHeader {
    @CFNullable
    private String haServerAddr;
    @CFNullable
    private String masterAddr;


    @Override
    public void checkFields() {

    }

    public String getHaServerAddr() {
        return haServerAddr;
    }

    public void setHaServerAddr(String haServerAddr) {
        this.haServerAddr = haServerAddr;
    }

    public String getMasterAddr() {
        return masterAddr;
    }

    public void setMasterAddr(String masterAddr) {
        this.masterAddr = masterAddr;
    }
}