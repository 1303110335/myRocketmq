/**
 * bianque.com
 * Copyright (C) 2013-2020All Rights Reserved.
 */
package com.rocketmq.remoting.test;

/**
 * @author xuleyan
 * @version RemoteCode.java, v 0.1 2020-11-29 11:09 上午
 */
public enum RemoteCode {
    REGISTER("1", "注册"),
    QUERY("2", "查询ip");
    private String code;

    private String name;

    RemoteCode(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static RemoteCode getByCode(String code) {
        for (RemoteCode value : RemoteCode.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}