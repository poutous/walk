package org.walkframework.base.system.task.export;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.walkframework.base.mvc.entity.TlMExportlog;
import org.walkframework.base.system.constant.CommonConstants;
import org.walkframework.base.system.task.AbstractClusterTask;
import org.walkframework.base.tools.spring.SpringContextHolder;
import org.walkframework.base.tools.spring.SpringPropertyHolder;
import org.walkframework.batis.dao.SqlSessionDao;
import org.walkframework.data.bean.PageData;
import org.walkframework.data.bean.Pagination;
import org.walkframework.data.entity.Conditions;

/**
 * 定时导出任务分发器
 * 
 * @author shf675
 *
 */
public class TimingExportDispenserTask extends AbstractClusterTask {
	
	/**
	 * 默认最大重试次数
	 */
	private final static Integer DEFAULT_MAX_RETRY_NUMS = 3;
	
	/**
	 * 默认过期时间
	 */
	private final static Long DEFAULT_EXPIRY_SECONDS = 86400L;
	
	/**
	 * 默认分批处理分页尺寸
	 */
	public final static int DEFAULT_PAGE_SIZE = 100;
	
	/**
	 * 重试次数
	 */
	private Integer maxRetryNums = DEFAULT_MAX_RETRY_NUMS;
	
	/**
	 * 过期时间，单位：秒
	 */
	private Long expirySeconds = DEFAULT_EXPIRY_SECONDS;
	
	/**
	 * dao工具
	 */
	private SqlSessionDao dao;
	
	@Override
	public void doExecute() {
		Conditions conditions = new Conditions(TlMExportlog.class);
		conditions.andEqual(TlMExportlog.EXPORT_MODE, CommonConstants.EXPORT_MODE_ASYN);
		conditions.andEqual(TlMExportlog.EXPORT_STATE, CommonConstants.EXPORT_STATE_APPOINTMENT);
		conditions.andIsNotNull(TlMExportlog.APPOINTMENT_TIME);
		conditions.andLess(TlMExportlog.RETRY_NUMS, getMaxRetryNums());
		
		// 分批加载，防止内存溢出
		int pageSize = getPageSize();
		int i = 0;
		for (;;) {
			Pagination pagination = new Pagination();
			pagination.setNeedCount(false);
			pagination.setRange(i * pageSize, pageSize);
			PageData<TlMExportlog> exportList = dao().selectList(conditions, pagination);
			List<TlMExportlog> rows = exportList.getRows();
			if(CollectionUtils.isEmpty(rows)){
				log.info("暂无导出任务...");
				return;
			}
			
			if(log.isInfoEnabled()){
				log.info("加载任务个数：{}", rows.size());
			}
			if (rows.size() > 0) {
				
				//业务逻辑处理
				List<TlMExportlog> updateExportLogs = new ArrayList<TlMExportlog>();
				for (final TlMExportlog exportLog : rows) {
					TlMExportlog eLog = new TlMExportlog();
					eLog.setLogId(exportLog.getLogId()).asCondition();
					updateExportLogs.add(eLog);
					
					long differTime = System.currentTimeMillis() - exportLog.getAppointmentTime().getTime();
					if(differTime > getExpirySeconds() * 1000){//检查是否过期
						eLog.setExportState(CommonConstants.EXPORT_STATE_FAILURE);
						eLog.setRemark("导出任务已过期");
					} else if(exportLog.getRetryNums() != null && exportLog.getRetryNums() >= getMaxRetryNums()) {//检查重试次数
						eLog.setExportState(CommonConstants.EXPORT_STATE_FAILURE);
						eLog.setRemark("导出任务已超过最大重试次数");
					} else {
						
						//发送消息
						sendMessage(exportLog.getChannelName(), exportLog.getLogId());
					}
				}
				
				//批量更新
				if(CollectionUtils.isNotEmpty(updateExportLogs)) {
					dao().updateBatch(updateExportLogs);
				}
				
				//中止
				if (rows.size() < pageSize) {
					break;
				}
			} else {
				break;
			}
			i++;
		}
	}
	
	/**
	 * 发送消息
	 * 
	 * @param channelName
	 * @param exportLogId
	 */
	@SuppressWarnings("unchecked")
	protected void sendMessage(String channelName, String exportLogId) {
		//导出任务入队
		getMasterFactory().getQueueManager().getQueue(TimingExportHelper.getQueueNameByChannel(channelName)).offer(exportLogId);
		//发送执行任务命令
		getMasterFactory().getPubSubManager().publish(1, channelName);
		if(log.isInfoEnabled()){
			log.info("导出任务已入队，任务ID：{}", exportLogId);
		}
	}
	
	public Integer getMaxRetryNums() {
		return maxRetryNums;
	}

	public void setMaxRetryNums(Integer maxRetryNums) {
		this.maxRetryNums = maxRetryNums;
	}
	
	public Long getExpirySeconds() {
		return expirySeconds;
	}

	public void setExpirySeconds(Long expirySeconds) {
		this.expirySeconds = expirySeconds;
	}
	
	public SqlSessionDao setDao(SqlSessionDao dao) {
		this.dao = dao;
		return this.dao;
	}
	
	public SqlSessionDao dao() {
		if(dao == null){
			dao = SpringContextHolder.getBean(SpringPropertyHolder.getContextProperty("walkbatis.defaultSqlSessionDaoName", "sqlSessionDao"), SqlSessionDao.class);
		}
		return dao;
	}
	
	public int getPageSize() {
		return Integer.parseInt(SpringPropertyHolder.getContextProperty("load.max.pagesize", DEFAULT_PAGE_SIZE + ""));
	}
}
