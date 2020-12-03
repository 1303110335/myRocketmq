/**
 * bianque.com
 * Copyright (C) 2013-2020All Rights Reserved.
 */
package com.rocketmq.remoting;

import com.rocketmq.remoting.protocol.RemotingCommand;

/**
 * @author xuleyan
 * @version RPCHook.java, v 0.1 2020-10-10 10:48 上午
 */
public interface RPCHook {

    void doBeforeRequest(final String remoteAddr, final RemotingCommand request);

    void doAfterResponse(final String remoteAddr, final RemotingCommand request, final RemotingCommand response);
}