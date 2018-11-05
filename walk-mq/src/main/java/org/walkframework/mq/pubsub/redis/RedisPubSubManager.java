package org.walkframework.mq.pubsub.redis;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.Assert;
import org.walkframework.mq.pubsub.IMessageListener;
import org.walkframework.mq.pubsub.IPubSubManager;

import redis.clients.jedis.Jedis;

/**
 * 基于redis实现的发布/订阅管理器
 * 
 * @author shf675
 * 
 */
@SuppressWarnings("unchecked")
public class RedisPubSubManager implements IPubSubManager {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private RedisSerializer<String> stringSerializer = new StringRedisSerializer();

	private final Map<IMessageListener, MessageListener> listeners = new ConcurrentHashMap<IMessageListener, MessageListener>();
	
	private RedisMessageListenerContainer container;

	private RedisOperations redisOperations;

	public void setRedisOperations(RedisOperations redisOperations) {
		this.redisOperations = redisOperations;
	}

	/**
	 * 支持消息发布到一个频道或多个频道
	 * 
	 * @param messageBody
	 * @param channels
	 */
	@Override
	public void publish(Object messageBody, String... channels) {
		Assert.notEmpty(channels, "at least one channel is required");

		Set<String> strSet = new HashSet<String>();
		Collections.addAll(strSet, channels);
		for (String channel : strSet) {
			this.redisOperations.convertAndSend(channel, messageBody);
		}
	}
	
	/**
	 * 支持订阅一个频道或多个频道，但非pattern模式
	 * 
	 * @param listener
	 * @param channels
	 */
	@Override
	public void subscribe(IMessageListener listener, String... channels) {
		container.addMessageListener(getMessageListener(listener, true), getTopicSet(ChannelTopic.class, channels));
	}
	
	/**
	 * 支持订阅一个频道或多个频道，pattern模式
	 * 
	 * @param listener
	 * @param channels
	 */
	@Override
	public void pSubscribe(IMessageListener listener, String... channels) {
		container.addMessageListener(getMessageListener(listener, true), getTopicSet(PatternTopic.class, channels));
	}

	/**
	 * 支持取消订阅一个频道或多个频道，但非pattern模式
	 * 
	 * @param listener
	 * @param channels
	 */
	@Override
	public void unsubscribe(IMessageListener listener, String... channels) {
		container.removeMessageListener(getMessageListener(listener, false), getTopicSet(ChannelTopic.class, channels));
	}
	
	/**
	 * 支持取消订阅一个频道或多个频道，pattern模式
	 * 
	 * @param listener
	 * @param channels
	 */
	@Override
	public void punsubscribe(IMessageListener listener, String... channels) {
		container.removeMessageListener(getMessageListener(listener, false), getTopicSet(PatternTopic.class, channels));
	}
	
	/**
	 * 返回当前的活跃频道
	 * 活跃频道指的是那些至少有一个订阅者的频道， 订阅模式的客户端不计算在内
	 * 
	 * @param pattern 可选参数
	 * 如果不给出 pattern 参数，那么列出订阅与发布系统中的所有活跃频道。
	 * 如果给出 pattern 参数，那么只列出和给定模式 pattern 相匹配的那些活跃频道。
	 * @return
	 */
	@Override
	public List<String> pubsubChannels(final String pattern) {
		return (List<String>) this.redisOperations.execute(new RedisCallback<List<String>>() {
			public List<String> doInRedis(RedisConnection connection) {
				Object nativeConnection = connection.getNativeConnection();
				List<String> channels = null;
				if(nativeConnection instanceof Jedis){//还有个jredis，这玩意好像没有实现pubsubChannels方法
					channels = ((Jedis)nativeConnection).pubsubChannels(pattern);
				}
				return channels;
			}
		});
	}
	
	/**
	 * 返回给定频道的订阅者数量，订阅模式的客户端不计算在内
	 * 
	 * @param channels
	 * @return key为频道，value为该频道订阅者数量
	 */
	@Override
	public Map<String, String> pubsubNumSub(final String... channels) {
		return (Map<String, String>) this.redisOperations.execute(new RedisCallback<Map<String, String>>() {
			public Map<String, String> doInRedis(RedisConnection connection) {
				Object nativeConnection = connection.getNativeConnection();
				Map<String, String> numbers = null;
				if(nativeConnection instanceof Jedis){//还有个jredis，这玩意好像没有实现pubsubNumSub方法
					numbers = ((Jedis)nativeConnection).pubsubNumSub(channels);
				}
				return numbers;
			}
		});
	}
	
	/**
	 * 返回订阅模式的数量
	 * 
	 * 注意， 这个命令返回的不是订阅模式的客户端的数量，而是客户端订阅的所有模式的数量总和。
	 * @return
	 */
	@Override
	public Long pubsubNumPat() {
		return (Long) this.redisOperations.execute(new RedisCallback<Long>() {
			public Long doInRedis(RedisConnection connection) {
				Object nativeConnection = connection.getNativeConnection();
				Long numbers = null;
				if(nativeConnection instanceof Jedis){//还有个jredis，这玩意好像没有实现pubsubNumPat方法
					numbers = ((Jedis)nativeConnection).pubsubNumPat();
				}
				return numbers;
			}
		});
	}
	
	/**
	 * 获取所有监听器
	 * 
	 * @return
	 */
	@Override
	public Map<IMessageListener, MessageListener> getListeners() {
		return listeners;
	}
	
	/**
	 * 获取messageListener
	 * 
	 * @param ilistener
	 * @return
	 */
	private MessageListener getMessageListener(final IMessageListener ilistener, boolean isNew) {
		MessageListener listener = listeners.get(ilistener);
		if (listener == null && isNew) {
			listener = new MessageListener() {
				@Override
				public void onMessage(final Message message, byte[] pattern) {
					handleMessage(ilistener, message, pattern);
				}
			};

			listeners.put(ilistener, listener);
		}
		return listener;
	}

	/**
	 * 消息处理
	 * 
	 * @param listener
	 * @param message
	 * @param pattern
	 */
	private void handleMessage(IMessageListener listener, Message message, byte[] pattern) {
		String channel = (String) stringSerializer.deserialize(message.getChannel());
		Object messageBody = redisOperations.getValueSerializer().deserialize(message.getBody());
		String pattn = (String) stringSerializer.deserialize(pattern);
		listener.onMessage(channel, messageBody, pattn);
	}

	/**
	 * 获取主题集合
	 * 
	 * @param channels
	 * @return
	 */
	private Set<Topic> getTopicSet(Class<? extends Topic> topicClazz, String... channels) {
		Assert.notEmpty(channels, "at least one channel is required");
		Set<String> strSet = new HashSet<String>();
		Collections.addAll(strSet, channels);

		Set<Topic> topics = new HashSet<Topic>();
		for (String channel : strSet) {
			try {
				topics.add(topicClazz.getConstructor(String.class).newInstance(channel));
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return topics;
	}

	public void setContainer(RedisMessageListenerContainer container) {
		this.container = container;
	}
	
}
