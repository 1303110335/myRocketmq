/**
 * bianque.com
 * Copyright (C) 2013-2020 All Rights Reserved.
 */
package com.rocketmq.namesrv.test;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author xuleyan
 * @version Test.java, v 0.1 2020-10-13 2:58 下午
 */
@Slf4j
public class Test {

    private static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public static void main(String[] args) {

        log.info("haha");
//        testWriteLockQueue();
//        testReadLockQueue();
    }

    public static void testWriteLockQueue() {
        for (int i = 0; i < 5; i++) {
            Thread thread = new Thread(() -> {
                lock.writeLock().lock();
                System.out.println("threadName = " + Thread.currentThread().getName() + "尝试获取锁");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.writeLock().unlock();
                    System.out.println("threadName = " + Thread.currentThread().getName() + "解锁成功");
                }
            });
            thread.start();
        }
    }

    public static void testReadLockQueue() {
        for (int i = 0; i < 5; i++) {
            Thread thread = new Thread(() -> {
                lock.readLock().lock();
                System.out.println("threadName = " + Thread.currentThread().getName() + "尝试获取锁");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.readLock().unlock();
                    System.out.println("threadName = " + Thread.currentThread().getName() + "解锁成功");
                }
            });
            thread.start();
        }
    }

    private static void testLock() {
        lock.writeLock().lock();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }
    }
}