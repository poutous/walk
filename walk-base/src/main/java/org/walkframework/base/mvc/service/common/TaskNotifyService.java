package org.walkframework.base.mvc.service.common;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.walkframework.base.mvc.entity.TfMNotify;
import org.walkframework.base.mvc.entity.TfMNotifyLog;
import org.walkframework.batis.bean.BatchEachHandler;
import org.walkframework.batis.dialect.OracleDialect;
import org.walkframework.data.bean.PageData;
import org.walkframework.data.bean.Pagination;

/**
 * 通知服务(任务专用)
 * 
 * @author shf675
 *
 */
@Service("taskNotifyService")
public class TaskNotifyService extends NotifyService {
	
	/**
	 * 查询通知队列
	 * 
	 * @param param
	 * @param pagination
	 * @return
	 */
	public PageData<TfMNotify> queryNotifyList(TfMNotify param, Pagination pagination) {
		if(dao().getDialect() instanceof OracleDialect){
			return dao().selectList("NotifySQL.queryNotifyList", param, pagination);
		}
		return dao().selectList("NotifySQL.queryNotifyList_".concat(dao().getDialect().getClass().getName().replaceAll("\\.", "_")), param, pagination);
	}
	
	/**
	 * 批量更新通知队列
	 * 
	 * @param notifyList
	 * @param batchEachHandler
	 * @param recordLog
	 */
	public void updateNotifyList(List<TfMNotify> notifyList, BatchEachHandler<TfMNotify> batchEachHandler, boolean recordLog) {
		//批量更新
		dao().updateBatch(notifyList, batchEachHandler);

		// 记录日志
		if(recordLog){
			insertNotifyLog(notifyList);
		}
	}
	
	/**
	 * 记录日志
	 * 
	 * @param notifyList
	 */
	protected void insertNotifyLog(List<TfMNotify> notifyList) {
		List<TfMNotifyLog> notifyLogList = new ArrayList<TfMNotifyLog>();
		for (TfMNotify tfMNotify : notifyList) {
			TfMNotifyLog log = new TfMNotifyLog();
			// 快速copy属性
			BeanUtils.copyProperties(tfMNotify, log);
			// 设置流水
			log.setLogId(getSequence("SEQ_LOG_ID"));
			// 设置日志创建时间
			log.setLogCreateTime(common.getCurrentTime());
			notifyLogList.add(log);
			
		}
		dao().insertBatch(notifyLogList);
	}

}
