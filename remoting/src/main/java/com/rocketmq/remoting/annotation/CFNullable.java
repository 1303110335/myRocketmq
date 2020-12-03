/**
 * bianque.com
 * Copyright (C) 2013-2020All Rights Reserved.
 */
package com.rocketmq.remoting.annotation;

import java.lang.annotation.*;

/**
 * @author xuleyan
 * @version CFNullable.java, v 0.1 2020-10-10 3:09 下午
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
public @interface CFNullable {

}