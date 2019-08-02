package org.walkframework.base.system.task.export;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.walkframework.base.mvc.service.common.AsynExportService;
import org.walkframework.base.tools.spring.SpringContextHolder;
import org.walkframework.mq.pubsub.IMessageListener;
import org.walkframework.mq.pubsub.Subscriber;
import org.walkframework.mq.queue.IQueue;
import org.walkframework.mq.queue.IQueueManager;

/**
 * 定时导出处理器监听
 *
 */
public class TimingExportExcuteMessageListener extends Subscriber implements IMessageListener {
	
	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private String queueManagerName;
	
	@Resource(name="asynExportService")
	private AsynExportService asynExportService;
	
	@SuppressWarnings("unchecked")
	@Override
	public void onMessage(String channel, Object messageBody, String pattern) {
		String exportLogId = null;
		try {
			IQueue<String> timingExportQueue = getQueueManager().getQueue(TimingExportHelper.getQueueNameByChannel(channel));
			
			//出队
			exportLogId = timingExportQueue.poll();
			if(StringUtils.isEmpty(exportLogId)){
				return;
			}
			
			if(log.isInfoEnabled()){
				log.info("任务ID{}已出队，开始执行导出...", exportLogId);
			}
			
			//执行导出
			asynExportService.doExecuteExport(exportLogId);
		} catch (Exception e) {
			log.error("执行导出意外失败：" + e.getMessage(), e);
			
			//重置导出状态
			if(exportLogId != null) {
				asynExportService.doResetExportState(exportLogId, e.getMessage());
			}
		}
	}
	
	public IQueueManager getQueueManager() {
		return SpringContextHolder.getBean(getQueueManagerName(), IQueueManager.class);
	}
	
	public String getQueueManagerName() {
		return queueManagerName;
	}

	public void setQueueManagerName(String queueManagerName) {
		this.queueManagerName = queueManagerName;
	}
	
	@Override
	public IMessageListener getMessageListener() {
		return this;
	}
}
