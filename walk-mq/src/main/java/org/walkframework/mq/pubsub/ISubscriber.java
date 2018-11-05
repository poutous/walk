package org.walkframework.mq.pubsub;

import java.util.Set;


/**
 * 订阅器
 * 
 * @author shf675
 * 
 */
public interface ISubscriber extends IChannel {

	/**
	 * 支持订阅一个频道或多个频道，支持pattern模式
	 * 
	 * @param listenerNumbers 指定启动监听个数
	 */
	void subscribe(int listenerNumbers);

	/**
	 * 支持取消订阅一个频道或多个频道，支持pattern模式
	 * 
	 */
	void unsubscribe();
	
	/**
	 * 获取本订阅器的消息监听集合
	 * 
	 */
	Set<IMessageListener> getMessageListeners();

	/**
	 * 获取所订阅的频道集合(pattern模式)
	 * 
	 * @return
	 */
	Set<String> getPatternChannels();
}
