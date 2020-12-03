/**
 * bianque.com
 * Copyright (C) 2013-2020 All Rights Reserved.
 */
package com.rocketmq.common.protocol;

import com.rocketmq.remoting.protocol.RemotingSysResponseCode;

/**
 * @author xuleyan
 * @version ResponseCode.java, v 0.1 2020-11-08 6:52 下午
 */
public class ResponseCode extends RemotingSysResponseCode {

    public static final int FLUSH_DISK_TIMEOUT = 10;

    public static final int SLAVE_NOT_AVAILABLE = 11;

    public static final int FLUSH_SLAVE_TIMEOUT = 12;
    /**
     * Message 不正确
     */
    public static final int MESSAGE_ILLEGAL = 13;

    public static final int SERVICE_NOT_AVAILABLE = 14;

    public static final int VERSION_NOT_SUPPORTED = 15;

    public static final int NO_PERMISSION = 16;
    /**
     * Topic 不存在
     */
    public static final int TOPIC_NOT_EXIST = 17;
    public static final int TOPIC_EXIST_ALREADY = 18;
    public static final int PULL_NOT_FOUND = 19;

    public static final int PULL_RETRY_IMMEDIATELY = 20;

    public static final int PULL_OFFSET_MOVED = 21;

    public static final int QUERY_NOT_FOUND = 22;

    public static final int SUBSCRIPTION_PARSE_FAILED = 23;
    /**
     * 订阅 不存在
     */
    public static final int SUBSCRIPTION_NOT_EXIST = 24;
    /**
     * 订阅 版本不不正确
     */
    public static final int SUBSCRIPTION_NOT_LATEST = 25;
    /**
     * 订阅分组 不存在
     */
    public static final int SUBSCRIPTION_GROUP_NOT_EXIST = 26;

    public static final int TRANSACTION_SHOULD_COMMIT = 200;

    public static final int TRANSACTION_SHOULD_ROLLBACK = 201;

    public static final int TRANSACTION_STATE_UNKNOW = 202;

    public static final int TRANSACTION_STATE_GROUP_WRONG = 203;
    public static final int NO_BUYER_ID = 204;

    public static final int NOT_IN_CURRENT_UNIT = 205;

    public static final int CONSUMER_NOT_ONLINE = 206;

    public static final int CONSUME_MSG_TIMEOUT = 207;

    public static final int NO_MESSAGE = 208;
}