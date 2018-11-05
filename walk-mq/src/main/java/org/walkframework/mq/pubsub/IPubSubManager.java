package org.walkframework.mq.pubsub;

import java.util.List;
import java.util.Map;

import org.springframework.data.redis.connection.MessageListener;


/**
 * 订阅/发布管理器
 * 
 * @author shf675
 *
 */
public interface IPubSubManager {
	
	/**
	 * 支持消息发布到一个频道或多个频道
	 * 
	 * @param messageBody
	 * @param channels
	 */
	void publish(Object messageBody, String... channels);
	
	/**
	 * 支持订阅一个频道或多个频道，但非pattern模式
	 * 
	 * @param listener
	 * @param channels
	 */
	void subscribe(IMessageListener listener, String... channels);
	
	/**
	 * 支持订阅一个频道或多个频道，pattern模式
	 * 
	 * @param listener
	 * @param channels
	 */
	void pSubscribe(IMessageListener listener, String... channels);

	/**
	 * 支持取消订阅一个频道或多个频道，但非pattern模式
	 * 
	 * @param listener
	 * @param channels
	 */
	void unsubscribe(IMessageListener listener, String... channels);
	
	/**
	 * 支持取消订阅一个频道或多个频道，pattern模式
	 * 
	 * @param listener
	 * @param channels
	 */
	void punsubscribe(IMessageListener listener, String... channels);
	
	
	/**
	 * 返回当前的活跃频道
	 * 活跃频道指的是那些至少有一个订阅者的频道， 订阅模式的客户端不计算在内
	 * 
	 * @param pattern 可选参数
	 * 如果不给出 pattern 参数，那么列出订阅与发布系统中的所有活跃频道。
	 * 如果给出 pattern 参数，那么只列出和给定模式 pattern 相匹配的那些活跃频道。
	 * @return
	 */
	List<String> pubsubChannels(final String pattern);
	
	/**
	 * 返回给定频道的订阅者数量，订阅模式的客户端不计算在内
	 * 
	 * @param channels
	 * @return key为频道，value为该频道订阅者数量
	 */
	Map<String, String> pubsubNumSub(final String... channels);
	
	/**
	 * 返回订阅模式的数量
	 * 
	 * 注意， 这个命令返回的不是订阅模式的客户端的数量，而是客户端订阅的所有模式的数量总和。
	 * @return
	 */
	Long pubsubNumPat();
	
	/**
	 * 获取所有监听器
	 * 
	 * @return
	 */
	Map<IMessageListener, MessageListener> getListeners();
}
