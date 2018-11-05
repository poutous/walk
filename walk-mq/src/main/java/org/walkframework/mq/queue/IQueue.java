package org.walkframework.mq.queue;

import java.util.Iterator;
import java.util.Queue;

/**
 * 基于Redis实现的消息队列
 * 
 * @author shf675
 * @param <E>
 */
public interface IQueue<E> extends Queue<E> {

	/**
	 * 获取队列名
	 * 
	 */
	public String getQueueName();

	/**
	 * 迭代器
	 * 
	 * 可指定范围
	 */
	public Iterator<E> iterator(final int start, final int size);
}