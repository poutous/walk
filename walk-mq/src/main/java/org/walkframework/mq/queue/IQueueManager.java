package org.walkframework.mq.queue;

import java.util.Set;


/**
 * 队列管理器接口
 * 
 * @author shf675
 * @param <T>
 */
@SuppressWarnings("unchecked")
public interface IQueueManager{
	
	/**
	 * 获取队列
	 * 
	 * @param queueName
	 * @return
	 */
	public IQueue getQueue(String queueName);
	
	/**
	 * 获取所有队列的名字
	 * @return
	 */
	public Set<String> getQueueNames();
}