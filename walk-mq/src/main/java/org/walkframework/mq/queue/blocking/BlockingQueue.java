package org.walkframework.mq.queue.blocking;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.walkframework.mq.queue.IQueue;



/**
 * 基于java自带的BlockingQueue实现的消息队列
 * 
 * 本地队列，web应用重启会清空队列，建议只在本地开发时使用
 * 
 * @author shf675
 * @param <T>
 */
@SuppressWarnings({ "unchecked", "serial" })
public class BlockingQueue<E> extends LinkedBlockingQueue<E> implements IQueue<E>{

	//队列名
	private String queueName;
	
	public BlockingQueue(String queueName){
		this.queueName = queueName;
	}

	/**
	 * 入队 如果队列已满，则返回false
	 * 
	 */
	@Override
	public boolean offer(E e) {
		return super.offer(e);
	}
	
	/**
	 * 出队 如果队列为空，则返回null
	 * 
	 */
	@Override
	public E poll() {
		return super.poll();
	}
	
	/**
	 * 返回队列头部的元素，但不删除 如果队列为空，则返回null
	 * 
	 */
	@Override
	public E peek() {
		return super.peek();
	}
	
	/**
	 * 获取队列大小
	 * 
	 */
	@Override
	public int size() {
		return super.size();
	}
	
	/**
	 * 迭代器
	 * 
	 */
	@Override
	public Iterator<E> iterator() {
		return super.iterator();
	}
	
	/**
	 * 迭代器
	 * 
	 * 可指定范围
	 */
	@Override
	public Iterator<E> iterator(final int start, final int size) {
		List<E> eles = new ArrayList<E>();
		Iterator<E> all = super.iterator();
		int i = 0;
		while (all.hasNext()) {
			if (i >= start && i < (start + size)) {
				eles.add(all.next());
			}
			i++;
		}
		return eles.iterator();
	}
	
	/**
	 * 获取队列名
	 * 
	 */
	@Override
	public String getQueueName() {
		return this.queueName;
	}
}