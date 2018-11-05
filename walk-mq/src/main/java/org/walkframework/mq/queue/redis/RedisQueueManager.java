package org.walkframework.mq.queue.redis;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.CollectionUtils;
import org.walkframework.mq.queue.IQueue;
import org.walkframework.mq.queue.IQueueManager;

/**
 * 基于Redis实现的消息队列管理器
 * 
 * 分布式队列，web应用重启不会清空队列，建议在生产使用
 * 
 * @author shf675
 * @param <T>
 */
@SuppressWarnings("unchecked")
public class RedisQueueManager implements IQueueManager, InitializingBean {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Map<String, IQueue> queueMap = new HashMap<String, IQueue>();

	private RedisSerializer<String> stringSerializer = new StringRedisSerializer();

	// 队列名集合
	private Set<String> queueNames = new HashSet<String>();

	private RedisOperations redisOperations;

	// 工程启动时是否加载远程队列名称。默认加载
	private boolean loadRemoteQueuesOnStartup = true;

	@Override
	public void afterPropertiesSet() throws Exception {
		if (isLoadRemoteQueuesOnStartup()) {
			Set<String> remoteQueues = loadRemoteQueues();
			if(!CollectionUtils.isEmpty(remoteQueues)){
				this.queueNames.addAll(loadRemoteQueues());
			}
		}
	}

	/**
	 * 获取队列
	 * 
	 * @param queueName
	 * @return
	 */
	public IQueue getQueue(String queueName) {
		IQueue queue = this.queueMap.get(queueName);
		if (queue == null) {
			queue = new RedisQueue(queueName, redisOperations, stringSerializer);
			this.queueMap.put(queueName, queue);
			this.queueNames.add(queueName);
		}
		return queue;
	}

	/**
	 * 从redis中加载队列名称
	 * 
	 * @return
	 */
	public Set<String> loadRemoteQueues() {
		return (Set<String>) redisOperations.execute(new RedisCallback<Set>() {
			public Set doInRedis(RedisConnection connection) throws DataAccessException {
				Set<String> queueSet = new HashSet<String>();
				byte[] queueSuffixByte = stringSerializer.serialize("*".concat(RedisQueue.QUEUE_SUFFIX));
				Set<byte[]> queueNames = connection.keys(queueSuffixByte);
				for (byte[] bs : queueNames) {
					String queue = (String) redisOperations.getKeySerializer().deserialize(bs);
					if (queue != null && queue.length() > 0) {
						queueSet.add(queue.substring(0, queue.length() - RedisQueue.QUEUE_SUFFIX.length()));
					}
				}
				return queueSet;
			}
		});
	}

	public void setQueueNames(Set<String> queueNames) {
		this.queueNames = queueNames;
	}

	public Set<String> getQueueNames() {
		return Collections.unmodifiableSet(this.queueNames);
	}

	public void setRedisOperations(RedisOperations redisOperations) {
		this.redisOperations = redisOperations;
	}

	public boolean isLoadRemoteQueuesOnStartup() {
		return loadRemoteQueuesOnStartup;
	}

	public void setLoadRemoteQueuesOnStartup(boolean loadRemoteQueuesOnStartup) {
		this.loadRemoteQueuesOnStartup = loadRemoteQueuesOnStartup;
	}

	public RedisOperations getRedisOperations() {
		return redisOperations;
	}
}