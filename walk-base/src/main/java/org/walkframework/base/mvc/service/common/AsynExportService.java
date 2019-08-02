package org.walkframework.base.mvc.service.common;

import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.stereotype.Service;
import org.walkframework.base.mvc.dao.BaseSqlSessionDao;
import org.walkframework.base.mvc.entity.ExportEntity;
import org.walkframework.base.mvc.entity.TlMExportlog;
import org.walkframework.base.system.constant.CommonConstants;
import org.walkframework.base.system.task.export.TimingExportExcuteMessageListener;
import org.walkframework.base.tools.spring.SpringContextHolder;
import org.walkframework.base.tools.spring.SpringPropertyHolder;
import org.walkframework.base.tools.utils.ExportUtil;
import org.walkframework.base.tools.utils.ReflectionUtils;
import org.walkframework.batis.dialect.OracleDialect;
import org.walkframework.data.bean.PageData;
import org.walkframework.shiro.subject.PrincipalHolder;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * 异步导出服务
 * 
 * @author shf675
 *
 */
@Service("asynExportService")
public class AsynExportService extends AbstractBaseService {
	
	private final static JdkSerializationRedisSerializer serializer = new JdkSerializationRedisSerializer(TimingExportExcuteMessageListener.class.getClassLoader());
	
	/**
	 * 执行导出
	 * 
	 * @throws Exception 
	 */
	public void doExecuteExport(String exportLogId) throws Exception {
		TlMExportlog exportLog = queryExportLogById(exportLogId);
		if(exportLog == null){
			throw new RuntimeException(String.format("任务ID%s不存在", exportLogId));
		}
		
		String param = exportLog.getParams();
		if(StringUtils.isEmpty(param)){
			throw new RuntimeException(String.format("任务ID%s参数为空！", exportLogId));	
		}
		
		//修改导出状态为导出中
		doUpdateStateExecuting(exportLog);
		
		//获取数据
		JSONObject paramJson = JSON.parseObject(param);
		Object data = getData(paramJson);
		
		if (data instanceof List || data instanceof PageData) {
			List<?> dataset = data != null && data instanceof PageData ? ((PageData<?>) data).getRows() : (List<?>) data;
			String total = (data != null && data instanceof PageData ? ((PageData<?>) data).getTotal() : ((List<?>) data).size()) + "";
			
			String headerJSON = paramJson.getString("headerJSON");
			String filePath = ExportUtil.writeZipFile(dataset, headerJSON, exportLog.getExportName());
			
			TlMExportlog export = new TlMExportlog();
			export.setLogId(exportLogId).asCondition();
			export.setExportPath(filePath);
			export.setTotal(new BigDecimal(total));
			
			//记录成功信息
			doRecordSuccess(export);
		}
	}
	
	/**
	 * 获取导出日志ID
	 * 
	 * @return
	 */
	public String getExportId() {
		String fileId = "";
		if (dao().getDialect() instanceof OracleDialect) {
			//oracle:序列+14位随机数
			fileId = ((BaseSqlSessionDao) dao()).getSequenceL16("SEQ_LOG_ID") + common.getRandomString(14);
		} else {
			//mysql或其他：使用UUID
			fileId = UUID.randomUUID().toString().replaceAll("-", "");
			int len = 40 - fileId.length();
			if (len > 0) {
				fileId += common.getRandomString(len);
			}
		}
		return fileId;
	}
	
	/**
	 * 根据导出ID获取导出对象
	 * 
	 * @param exportLogId
	 * @return
	 */
	@SuppressWarnings("serial")
	public TlMExportlog queryExportLogById(final String exportLogId) {
		return dao().selectOne(new TlMExportlog(){{
			setLogId(exportLogId).asCondition();
		}});
	}
	
	/**
	 * 创建导出任务
	 * 
	 * @param exportName
	 * @throws Exception 
	 */
	public TlMExportlog doCreateExport(ExportEntity exportEntity) throws Exception {
		String exportId = getExportId();
		TlMExportlog export = new TlMExportlog();
		export.setLogId(exportId);
		export.setExportName(exportEntity.getExportName());
		export.setExportMode(CommonConstants.EXPORT_MODE_ASYN);
		export.setExportState(CommonConstants.EXPORT_STATE_EXECUTING);
		
		export.setReqUri(exportEntity.getReqUri());
		export.setParams(exportEntity.getParams());
		export.setOperateIp(exportEntity.getOperateIp());
		export.setCreateStaff(exportEntity.getOperateStaff());
		export.setCreateTime(common.getCurrentTime());
		
		//设置预约时间
		if(exportEntity.getAppointmentTime() != null){
			export.setExportState(CommonConstants.EXPORT_STATE_APPOINTMENT);
			export.setAppointmentTime(common.encodeTimestamp(exportEntity.getAppointmentTime()));
			
			//定时导出频道名称
			String timingexportChannel = SpringPropertyHolder.getContextProperty("export.timing.channel");
			if(StringUtils.isEmpty(timingexportChannel)) {
				common.error("错误：application.properties文件中未定义定时导出消息通道export.timing.channel");
			}
			export.setChannelName(timingexportChannel);
		}
		
		dao().insert(export);
		return export;
	}
	
	/**
	 * 记录成功信息
	 * 
	 * @param succ
	 */
	public void doRecordSuccess(TlMExportlog succ) {
		succ.setLogId(succ.getLogId()).asCondition();
		succ.setExportState(CommonConstants.EXPORT_STATE_SUCCESS);
		succ.setFinishTime(new Date());
		succ.setFileSize(new BigDecimal(new File(succ.getExportPath()).length()));
		dao().update(succ);
	}

	/**
	 * 记录失败信息
	 * 
	 * @param exportId
	 * @param errMsg
	 */
	public void doRecordFailure(String exportId, String errMsg) {
		TlMExportlog err = new TlMExportlog();
		err.setLogId(exportId).asCondition();
		err.setExportState(CommonConstants.EXPORT_STATE_FAILURE);
		err.setFinishTime(common.getCurrentTime());
		err.setErrorInfo(errMsg);
		dao().update(err);
	}
	
	/**
	  * 重置导出状态
	 * 
	 * @param exportId
	 */
	public void doResetExportState(String exportId, String errMsg) {
		TlMExportlog exportlog = new TlMExportlog();
		exportlog.setLogId(exportId).asCondition();
		exportlog.setExportState(CommonConstants.EXPORT_STATE_APPOINTMENT);
		exportlog.setErrorInfo(errMsg);
		dao().update(exportlog);
	}
	
	/**
	 * 修改导出状态为导出中
	 * 
	 * @param exportId
	 */
	public void doUpdateStateExecuting(TlMExportlog eLog) {
		TlMExportlog exportlog = new TlMExportlog();
		exportlog.setLogId(eLog.getLogId()).asCondition();
		exportlog.setExportState(CommonConstants.EXPORT_STATE_EXECUTING);
		exportlog.setRetryNums(eLog.getRetryNums() == null ? 1 : eLog.getRetryNums() + 1);
		dao().update(exportlog);
	}
	
	/**
	 * 获取数据
	 * 
	 * @param paramJson
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	private Object getData(JSONObject paramJson) throws Exception {
		String className = paramJson.getString("className");
		String methodName = paramJson.getString("methodName");
		String exportParams = paramJson.getString("exportParams");
		String exportParamsClass = paramJson.getString("exportParamsClass");
		
		//设置身份信息
		Object principal = serializer.deserialize(Hex.decodeHex(paramJson.getString("principal").toCharArray()));
		PrincipalHolder.setPrincipal(principal);
		
		List exportParamList = exportParams == null ? null:(List)serializer.deserialize(Hex.decodeHex(exportParams.toCharArray()));
		Class[] exportParamsClazz = exportParamsClass == null ? null:(Class[])serializer.deserialize(Hex.decodeHex(exportParamsClass.toCharArray()));
		
		//反射执行获取数据
		Object bean = SpringContextHolder.getBean(Class.forName(className));
		return ReflectionUtils.invoke(bean, methodName, exportParamList.toArray(), exportParamsClazz);
	}
	
}
