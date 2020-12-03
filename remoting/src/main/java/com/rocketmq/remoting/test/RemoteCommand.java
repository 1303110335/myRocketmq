/**
 * bianque.com
 * Copyright (C) 2013-2020 All Rights Reserved.
 */
package com.rocketmq.remoting.test;

import com.rocketmq.remoting.protocol.RemotingCommandType;
import com.rocketmq.remoting.protocol.RemotingSysResponseCode;
import com.rocketmq.remoting.protocol.SerializeType;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author xuleyan
 * @version RemoteCommand.java, v 0.1 2020-11-29 8:02 下午
 */
public class RemoteCommand implements Serializable {

    private static final long serialVersionUID = -3858522208371266291L;

    /**
     * 内容
     */
    private byte[] body;

    /**
     * 1： 注册，2：查询，3：存储
     */
    private String requestType;

    /**
     * @see RemotingSysResponseCode
     */
    private int code;

    private static AtomicInteger requestId = new AtomicInteger(0);
    private int opaque = requestId.getAndIncrement();

    private RemotingCommandType type;

    private String ipAddress;

    public static Object decode(final ByteBuffer byteBuffer) {

        byte[] body = new byte[byteBuffer.limit()];
        if (byteBuffer.limit() > 0) {
            byteBuffer.get(body);
        }
        RemoteCommand remoteCommand = new RemoteCommand();
        remoteCommand.setBody(body);
        return remoteCommand;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public RemotingCommandType getType() {
        return type;
    }

    public void setType(RemotingCommandType type) {
        this.type = type;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getOpaque() {
        return opaque;
    }

    public void setOpaque(int opaque) {
        this.opaque = opaque;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    @Override
    public String toString() {
        return "RemoteCommand{" +
                "body=" + new String(body, StandardCharsets.UTF_8) +
                ", requestType='" + requestType + '\'' +
                ", code=" + code +
                ", opaque=" + opaque +
                ", type=" + type +
                ", ipAddress='" + ipAddress + '\'' +
                '}';
    }
}