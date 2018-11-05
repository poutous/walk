package org.walkframework.base.mvc.service.common;

import java.util.List;

import org.walkframework.base.system.task.notify.NotifyData;

public interface INotifyService {
	
	/**
	 * 批量插入通知队列
	 * 
	 * @param notifyList
	 */
	void insertNotifyList(String serviceId, List<NotifyData> notifyList);

}
