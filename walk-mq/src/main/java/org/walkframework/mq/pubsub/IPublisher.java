package org.walkframework.mq.pubsub;

/**
 * 发布器
 * 
 * @author shf675
 *
 */
public interface IPublisher extends IChannel {

	/**
	 * 支持消息发布到一个频道或多个频道
	 * 
	 * @param messageBody
	 */
	void publish(Object messageBody);
}
