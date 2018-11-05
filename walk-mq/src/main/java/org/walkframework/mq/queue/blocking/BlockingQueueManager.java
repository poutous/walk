package org.walkframework.mq.queue.blocking;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.walkframework.mq.queue.IQueue;
import org.walkframework.mq.queue.IQueueManager;

/**
 * 基于java自带的BlockingQueue实现的消息队列管理器
 * 
 * 本地队列，web应用重启会清空队列，建议只在本地开发时使用
 * 
 * @author shf675
 * @param <T>
 */
@SuppressWarnings("unchecked")
public class BlockingQueueManager implements IQueueManager{
	
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final Map<String, IQueue> queueMap = new HashMap<String, IQueue>();

	//队列名集合
	private Set<String> queueNames = new HashSet<String>();
	
	public void setQueueNames(Set<String> queueNames) {
		this.queueNames = queueNames;
	}
	
	/**
	 * 获取队列
	 * 
	 * @param queueName
	 * @return
	 */
	public IQueue getQueue(String queueName){
		IQueue queue = this.queueMap.get(queueName);
		if(queue == null){
			queue = new BlockingQueue(queueName);
			this.queueMap.put(queueName, queue);
			this.queueNames.add(queueName);
		}
		return queue;
	}
	
	/**
	 * 获取所有队列名字
	 * 
	 * @param queueName
	 * @return
	 */
	public Set<String> getQueueNames() {
		return Collections.unmodifiableSet(this.queueNames);
	}
	
}