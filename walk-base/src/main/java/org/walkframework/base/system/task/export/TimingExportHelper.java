package org.walkframework.base.system.task.export;

public class TimingExportHelper {
	
	/**
	 * 根据频道名称获取
	 * 
	 * @param channelName
	 * @return
	 */
	public static String getQueueNameByChannel(String channelName) {
		return channelName + "_QUEUE";
	}
	
}
