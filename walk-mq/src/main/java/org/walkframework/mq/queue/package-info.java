/**
 * 1、基于java自带的BlockingQueue实现的消息队列管理器，本地队列，web应用重启会清空队列，建议只在本地开发时使用
 * 2、基于Redis实现的消息队列管理器，分布式队列，web应用重启不会清空队列，建议在生产使用
 */
package org.walkframework.mq.queue;

