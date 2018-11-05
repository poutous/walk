package org.walkframework.base.system.constant;

public interface NotifyConstants {
	
	/**
	 * 通知模式：消息(mq)
	 */
	String NOTIFY_MODE_MQ = "01";
	
	/**
	 * 通知模式：URL通知(业务平台提供通知URL)
	 */
	String NOTIFY_MODE_URL = "02";
	
	
	/**
	 * 通知类型：立即通知
	 */
	String NOTIFY_TYPE_IMMEDIATELY = "01";
	
	
	/**
	 * 通知类型：周期性通知
	 */
	String NOTIFY_TYPE_CYCLE = "02";
	
	
}
