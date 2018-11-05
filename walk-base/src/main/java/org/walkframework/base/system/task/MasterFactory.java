package org.walkframework.base.system.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisOperations;
import org.walkframework.base.tools.spring.SpringContextHolder;
import org.walkframework.cache.ICache;
import org.walkframework.cache.ICacheManager;
import org.walkframework.mq.pubsub.IPubSubManager;
import org.walkframework.mq.queue.IQueueManager;
import org.walkframework.redis.lock.LockCallback;
import org.walkframework.redis.lock.RedisLock;

/**
 * 基于缓存的master帮助类
 * 
 * @author shf675
 *
 */
public class MasterFactory {

	protected static Logger log = LoggerFactory.getLogger(MasterFactory.class);

	//是否启用集群任务
	private boolean cluster;

	//缓存管理器名字
	private ICacheManager cacheManager;

	//队列管理器名字
	private String queueManagerName;

	//发布订阅器名字
	private String pubSubManagerName;

	/**
	 * 是否是master
	 * 
	 * @return
	 */
	public boolean isMaster() {
		ICache healthCache = cacheManager.getICache(TaskConstants.TASK_HEALTH_CACHE_NAME);
		return TaskConstants.UU_ID.equals(healthCache.getValue(TaskConstants.TASK_HEALTH_KEY_NAME));
	}

	/**
	 * 集群中是否有master
	 * 
	 * @return
	 */
	public boolean existMaster() {
		ICache healthCache = cacheManager.getICache(TaskConstants.TASK_HEALTH_CACHE_NAME);
		return healthCache.getValue(TaskConstants.TASK_HEALTH_KEY_NAME) != null;
	}

	/**
	 * 尝试锁定本服务作为master
	 * 
	 * @param timeoutMillis
	 */
	@SuppressWarnings("unchecked")
	public void tryLockMaster(final long timeoutMillis) {
		final ICache healthCache = cacheManager.getICache(TaskConstants.TASK_HEALTH_CACHE_NAME);
		if (healthCache.getNativeCache() instanceof RedisOperations) {
			RedisOperations redisOperations = (RedisOperations) healthCache.getNativeCache();
			
			//利用redis全局锁
			RedisLock lock = new RedisLock(redisOperations, TaskConstants.TASK_HEALTH_CACHE_NAME);
			lock.execute(new LockCallback<Boolean>() {
				@Override
				public Boolean doInLock(RedisConnection connection) {

					if (!existMaster() || isMaster()) {
						//将全局唯一ID置入缓存
						healthCache.put(TaskConstants.TASK_HEALTH_KEY_NAME, TaskConstants.UU_ID);

						//设置过期时间
						healthCache.expire(TaskConstants.TASK_HEALTH_KEY_NAME, timeoutMillis / 1000);

						log.info("I'm master! UUID[{}]", TaskConstants.UU_ID);
					}
					return true;
				}
			});
		}
	}

	public ICacheManager getCacheManager() {
		return cacheManager;
	}

	public void setCacheManager(ICacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	public boolean isCluster() {
		return cluster;
	}

	public void setCluster(boolean cluster) {
		this.cluster = cluster;
	}

	public IQueueManager getQueueManager() {
		return SpringContextHolder.getBean(getQueueManagerName(), IQueueManager.class);
	}

	public IPubSubManager getPubSubManager() {
		return SpringContextHolder.getBean(getPubSubManagerName(), IPubSubManager.class);
	}

	public String getQueueManagerName() {
		return queueManagerName;
	}

	public void setQueueManagerName(String queueManagerName) {
		this.queueManagerName = queueManagerName;
	}

	public String getPubSubManagerName() {
		return pubSubManagerName;
	}

	public void setPubSubManagerName(String pubSubManagerName) {
		this.pubSubManagerName = pubSubManagerName;
	}
}
