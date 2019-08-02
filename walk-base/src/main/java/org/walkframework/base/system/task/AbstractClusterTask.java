package org.walkframework.base.system.task;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisOperations;
import org.walkframework.base.system.common.Common;
import org.walkframework.base.system.factory.SingletonFactory;
import org.walkframework.mq.pubsub.IMessageListener;
import org.walkframework.mq.pubsub.Subscriber;
import org.walkframework.mq.queue.IQueue;
import org.walkframework.mq.queue.IQueueManager;
import org.walkframework.mq.queue.redis.RedisQueueManager;
import org.walkframework.redis.lock.LockCallback;
import org.walkframework.redis.lock.RedisLock;

/**
 * 集群任务
 * 
 * @author shf675
 *
 */
public abstract class AbstractClusterTask extends Subscriber implements IMessageListener, ClusterTask {

	protected Logger log = LoggerFactory.getLogger(this.getClass());

	protected Common common = SingletonFactory.getInstance(Common.class);

	//本任务频道名称
	private final String CURRENT_CHANNEL = "TASK_CHANNEL:" + this.getClass().getName();

	//本任务队列名称
	private final String CURRENT_QUEUE = "TASK_QUEUE:" + this.getClass().getName();

	//本任务锁名称
	private final String CURRENT_TASK_LOCK_NAME = "CURRENT_TASK_LOCK:" + this.getClass().getName();

	private MasterFactory masterFactory;

	@Override
	public void afterPropertiesSet() throws Exception {
		if (getMasterFactory().isCluster()) {
			//开始订阅
			subscribe(getListenerNumbers());
		}
	}

	/**
	 * 集群中本任务只有一个服务(master)在执行
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public void doTask() {
		MasterFactory masterFactory = getMasterFactory();
		//如果使用集群模式则发送消息
		if (masterFactory.isCluster()) {
			if (masterFactory.isMaster()) {
				//令牌入队。使用队列保证集群中同时只有一个服务执行任务，即集群中只有拿到令牌的服务才会执行任务
				masterFactory.getQueueManager().getQueue(CURRENT_QUEUE).offer(1);

				//发送执行任务命令
				masterFactory.getPubSubManager().publish(1, CURRENT_CHANNEL);
			}
		}

		//否则直接执行任务
		else {
			doExecute();
		}
	}

	/**
	 * 接收消息并处理
	 * 
	 * @param channel
	 * @param messageBody
	 * @param pattern
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public void onMessage(String channel, Object messageBody, String pattern) {
		if (CURRENT_CHANNEL.equals(channel)) {
			//使用队列保证集群中同时只有一个服务执行任务
			IQueueManager queueManager = masterFactory.getQueueManager();
			IQueue queue = queueManager.getQueue(CURRENT_QUEUE);
			if (queue.poll() != null) {

				//同步执行任务，即同一个任务直到上一次执行完成时再执行下次。单机运行时，spring task内部已经实现，但集群任务下需要另外实现，此处基于redis全局锁实现
				if (queueManager instanceof RedisQueueManager) {

					//基于redis全局锁实现
					RedisOperations redisOperations = ((RedisQueueManager) queueManager).getRedisOperations();
					RedisLock lock = new RedisLock(redisOperations, CURRENT_TASK_LOCK_NAME);
					lock.execute(new LockCallback<Boolean>() {
						@Override
						public Boolean doInLock(RedisConnection connection) {

							//执行具体的任务
							doExecute();

							return true;
						}
					});
				}
				//其他方式待实现...
				else {
					doExecute();
				}

			}
		}
	}

	@Override
	public IMessageListener getMessageListener() {
		return this;
	}

	@Override
	public Set<String> getChannels() {
		Set<String> channels = new HashSet<String>();
		channels.add(CURRENT_CHANNEL);
		return channels;
	}

	public MasterFactory getMasterFactory() {
		return masterFactory;
	}

	public void setMasterFactory(MasterFactory masterFactory) {
		this.masterFactory = masterFactory;
	}
}
