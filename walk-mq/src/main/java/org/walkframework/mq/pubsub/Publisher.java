package org.walkframework.mq.pubsub;

import java.util.Set;

import org.springframework.util.Assert;

/**
 * 发布器
 * 
 * @author shf675
 *
 */
public class Publisher implements IPublisher {
	
	private IPubSubManager pubSubManager;
	
	private Set<String> channels;
	

	/**
	 * 支持消息发布到一个频道或多个频道
	 * 
	 * @param messageBody
	 */
	@Override
	public void publish(Object messageBody){
		Assert.notEmpty(this.channels, "at least one channel is required");
		getPubSubManager().publish(messageBody, this.channels.toArray(new String[this.channels.size()]));
	}
	
	/**
	 * 获取本发布器频道集合(非pattern模式)
	 * 
	 * @return
	 */
	@Override
	public Set<String> getChannels() {
		return channels;
	}
	
	public void setChannels(Set<String> channels) {
		this.channels = channels;
	}

	public IPubSubManager getPubSubManager() {
		return pubSubManager;
	}

	public void setPubSubManager(IPubSubManager pubSubManager) {
		this.pubSubManager = pubSubManager;
	}
}
