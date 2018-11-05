package org.walkframework.mq.pubsub;

import java.util.Set;

/**
 * 频道集合
 * 
 * @author shf675
 *
 */
public interface IChannel {
	
	/**
	 * 获取所订阅的频道集合
	 * 
	 * @return
	 */
	Set<String> getChannels();
	
}
