package org.walkframework.mq.pubsub;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.CollectionUtils;

import com.rits.cloning.Cloner;

/**
 * 订阅器
 * 
 * @author shf675
 *
 */
public class Subscriber implements ISubscriber, InitializingBean {
	
	/**
	 * 默认监听数量
	 */
	private static final int DEFAULT_LISTENER_NUMBERS = 1;

	private IPubSubManager pubSubManager;

	private Set<String> channels;

	private Set<String> patternChannels;

	private IMessageListener messageListener;
	
	//是否立即订阅，默认是立即订阅
	private boolean isImmediatelySubscribe = true;
	
	//启动监听个数
	private int listenerNumbers = DEFAULT_LISTENER_NUMBERS;
	
	private final Set<IMessageListener> messageListeners = new HashSet<IMessageListener>();
	
	private static final Cloner cloner = new Cloner();

	/**
	 * Spring配置方式自动订阅
	 * 
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		if(this.isImmediatelySubscribe){
			//开始订阅
			subscribe(this.listenerNumbers);
		}
	}

	/**
	 * 支持订阅一个频道或多个频道，支持pattern模式
	 * 
	 * @param listenerNumbers 指定启动监听个数
	 */
	@Override
	public void subscribe(int listenerNumbers) {
		Set<String> channels = getChannels();
		Set<String> patternChannels = getPatternChannels();
		
		if (CollectionUtils.isEmpty(channels) && CollectionUtils.isEmpty(patternChannels)) {
			throw new IllegalArgumentException("at least one channel is required");
		}
		IMessageListener messageListener = getMessageListener();
		if(messageListener == null){
			throw new IllegalArgumentException("not configured message listener");
		}
		for (int i = 0; i < listenerNumbers; i++) {
			IMessageListener clonedMessageListener = cloneMessageListener(messageListener);
			this.messageListeners.add(clonedMessageListener);
			
			if (!CollectionUtils.isEmpty(channels)) {
				getPubSubManager().subscribe(clonedMessageListener, channels.toArray(new String[channels.size()]));
			}
			if (!CollectionUtils.isEmpty(patternChannels)) {
				getPubSubManager().pSubscribe(clonedMessageListener, patternChannels.toArray(new String[patternChannels.size()]));
			}
		}
	}

	/**
	 * 支持取消订阅一个频道或多个频道，支持pattern模式
	 * 
	 */
	@Override
	public void unsubscribe() {
		Set<String> channels = getChannels();
		Set<String> patternChannels = getPatternChannels();
		
		if (CollectionUtils.isEmpty(channels) && CollectionUtils.isEmpty(patternChannels)) {
			throw new IllegalArgumentException("at least one channel is required");
		}
		for (IMessageListener messageListener : this.messageListeners) {
			if (!CollectionUtils.isEmpty(channels)) {
				getPubSubManager().unsubscribe(messageListener, channels.toArray(new String[channels.size()]));
			}
			
			if (!CollectionUtils.isEmpty(patternChannels)) {
				getPubSubManager().punsubscribe(messageListener, patternChannels.toArray(new String[patternChannels.size()]));
			}
		}
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

	/**
	 * 获取本发布器频道集合(pattern模式)
	 * 
	 * @return
	 */
	@Override
	public Set<String> getPatternChannels() {
		return patternChannels;
	}
	
	/**
	 * 获取本订阅器的消息监听集合
	 * 
	 */
	@Override
	public Set<IMessageListener> getMessageListeners() {
		return messageListeners;
	}
	
	/**
	 * 克隆监听器
	 * 
	 * @param messageListener
	 * @return
	 */
	private IMessageListener cloneMessageListener(IMessageListener messageListener) {
		if(getListenerNumbers() == 1){
			return messageListener;
		}
		IMessageListener newMessageListener = cloner.fastCloneOrNewInstance(messageListener.getClass());
    	cloner.copyPropertiesOfInheritedClass(messageListener, newMessageListener);
        return newMessageListener;
    }

	public void setMessageListener(IMessageListener messageListener) {
		this.messageListener = messageListener;
	}

	public IPubSubManager getPubSubManager() {
		return pubSubManager;
	}

	public void setPubSubManager(IPubSubManager pubSubManager) {
		this.pubSubManager = pubSubManager;
	}

	public void setPatternChannels(Set<String> patternChannels) {
		this.patternChannels = patternChannels;
	}

	public void setChannels(Set<String> channels) {
		this.channels = channels;
	}
	
	public void setIsImmediatelySubscribe(boolean isImmediatelySubscribe) {
		this.isImmediatelySubscribe = isImmediatelySubscribe;
	}

	public void setListenerNumbers(int listenerNumbers) {
		this.listenerNumbers = listenerNumbers;
	}

	public IMessageListener getMessageListener() {
		return messageListener;
	}

	public int getListenerNumbers() {
		return listenerNumbers;
	}
}
