package org.walkframework.mq.pubsub;

/**
 * 消息监听器
 * 
 * @author shf675
 *
 */
public interface IMessageListener {
	
	/**
	 * 当接收到消息后进行处理
	 * 
	 * @param channel
	 * @param messageBody
	 * @param pattern
	 */
	void onMessage(String channel, Object messageBody, String pattern);
}
