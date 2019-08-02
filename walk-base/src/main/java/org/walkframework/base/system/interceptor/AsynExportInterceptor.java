package org.walkframework.base.system.interceptor;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.walkframework.base.mvc.entity.ExportEntity;
import org.walkframework.base.mvc.entity.TlMExportlog;
import org.walkframework.base.mvc.service.common.AsynExportService;
import org.walkframework.base.system.annotation.DataExport;
import org.walkframework.base.system.common.Common;
import org.walkframework.base.system.constant.CommonConstants;
import org.walkframework.base.system.factory.SingletonFactory;
import org.walkframework.base.system.task.export.TimingExportExcuteMessageListener;
import org.walkframework.base.tools.spring.SpringPropertyHolder;
import org.walkframework.base.tools.utils.ExportUtil;
import org.walkframework.data.bean.PageData;
import org.walkframework.data.util.InParam;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * spring校验结果拦截器
 * 
 * @author shf675
 *
 */
public class AsynExportInterceptor {
	
	protected final static Common common = SingletonFactory.getInstance(Common.class);

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private final static JdkSerializationRedisSerializer serializer = new JdkSerializationRedisSerializer(TimingExportExcuteMessageListener.class.getClassLoader());
	
	/**
	 * 线程池默认大小100
	 */
	private int DEFAULT_POOL_SIZE = 100;
	
	/**
	 * 线程池
	 */
	private ExecutorService threadPool;
	
	/**
	 * 线程池大小
	 */
	private int threadPoolSize = DEFAULT_POOL_SIZE;
	
	@Autowired
	HttpServletRequest request;

	@Autowired
	HttpServletResponse response;
	
	@Resource(name="asynExportService")
	private AsynExportService asynExportService;

	/**
	 * 环绕方法
	 * 
	 * @param pjp
	 * @param bindingResult
	 * @return
	 * @throws Throwable
	 */
	public Object doAround(final ProceedingJoinPoint pjp) throws Throwable {
		
		//如果是消息发起则直接返回
		String actionType = null;
		try {
			actionType = request.getParameter(CommonConstants.ACTION_TYPE);
		} catch (IllegalStateException e) {
			return pjp.proceed();
		} catch (Exception e) {
			return pjp.proceed();
		}
		
		InParam<String, Object> inParam = common.getInParam(request);
		
		//导出方式
		final String asynExportWay = inParam.getString(CommonConstants.ASYN_EXPORT_WAY);
		if("0".equals(asynExportWay)) {
			common.error("错误：系统暂时禁止导出操作！");
		}
		
		if (CommonConstants.ACTION_TYPE_ASYN_EXPORT.equals(actionType)) {
			
			//导出前校验
			preCheck(inParam);
			
			//导出文件名称
			final String exportName = inParam.getString(CommonConstants.ASYN_EXPORT_NAME);
			
			//导出表头
			final String headerJSON = inParam.getString(CommonConstants.HEADER_JSON_NAME);
			
			ExportEntity exportEntity = new ExportEntity();
			exportEntity.setExportName(exportName);
			exportEntity.setReqUri(request.getRequestURI());
			exportEntity.setParams(JSON.toJSONString(inParam));
			exportEntity.setOperateIp(common.getIpAddr(request));
			exportEntity.setOperateStaff((String)common.getValueByFieldName(SecurityUtils.getSubject().getPrincipal(), "userId"));

			//记录入库
			TlMExportlog exportLog = null;

			//定时导出
			if(CommonConstants.EXPORT_WAY_TIMING.equals(asynExportWay)) {
				//预约导出时间
				final String appointmentTime = inParam.getString(CommonConstants.ASYN_EXPORT_APPOINTMENT_TIME);
				exportEntity.setAppointmentTime(appointmentTime);
				
				//设置导出参数
				setExportParams(pjp, exportEntity, headerJSON);
				
				//创建导出任务
				exportLog = asynExportService.doCreateExport(exportEntity);
			} 
			//即时导出：开启新的线程进行异步导出
			else {
				exportLog = asynExportService.doCreateExport(exportEntity);
				
				final String exportId = exportLog.getLogId();
				getThreadPool().execute(new Runnable() {
					@Override
					public void run() {
						try {
							//导出
							TlMExportlog elog = doExport(pjp, headerJSON, exportName);
							elog.setLogId(exportId);
							
							//记录成功信息
							asynExportService.doRecordSuccess(elog);
						} catch (Throwable e) {
							log.error("导出失败[" + exportId + "]", e);
							
							//记录失败信息
							asynExportService.doRecordFailure(exportId, e.getMessage());
						}
					}
				});
			}

			//返回导出流水
			response.setHeader(CommonConstants.ASYN_EXPORT_ID, exportLog.getLogId());

			//异步导出返回null
			return null;
		}
		return pjp.proceed();
	}
	
	/**
	  *  导出前校验
	 * 
	 * @param inParam
	 */
	private void preCheck(InParam<String, Object> inParam) {
		//导出文件名称
		final String exportName = inParam.getString(CommonConstants.ASYN_EXPORT_NAME);
		if(StringUtils.isEmpty(exportName)) {
			common.error("错误：请输入导出文件名称！");
		}
		
		//导出方式
		final String asynExportWay = inParam.getString(CommonConstants.ASYN_EXPORT_WAY);
		if(StringUtils.isEmpty(asynExportWay)) {
			common.error("错误：导出方式(即时导出/预约导出)未指定！");
		}
		
		String asynExportWayConfig = SpringPropertyHolder.getContextProperty("export.asynExportWay", "1");
		if(!asynExportWay.matches(asynExportWayConfig)) {
			common.error(String.format("错误：不支持的导出方式[%s]，系统支持列表[%s]", asynExportWay, asynExportWayConfig));
		}
		
		if(CommonConstants.EXPORT_WAY_TIMING.equals(asynExportWay)) {
			//预约导出时间
			final String appointmentTime = inParam.getString(CommonConstants.ASYN_EXPORT_APPOINTMENT_TIME);
			if(StringUtils.isEmpty(appointmentTime)) {
				common.error("错误：请选择预约导出时间！");
			}
		}
	}
	
	/**
	 * 导出
	 * 
	 * @param obj
	 * @throws Throwable 
	 */
	private TlMExportlog doExport(ProceedingJoinPoint pjp, String headerJSON, String exportName) throws Throwable {
		String filePath = null;
		String total = "0";
		
		//获取原数据
		Object data = pjp.proceed();
		if (data instanceof List || data instanceof PageData) {
			List<?> dataset = data != null && data instanceof PageData ? ((PageData<?>) data).getRows() : (List<?>) data;
			total = (data != null && data instanceof PageData ? ((PageData<?>) data).getTotal() : ((List<?>) data).size()) + "";
			
			Method targetMethod = ((MethodSignature) pjp.getSignature()).getMethod();
			DataExport de = targetMethod.getAnnotation(DataExport.class);
			if (de != null && !StringUtils.isEmpty(de.xml())) {
				filePath = ExportUtil.writeZipFileByXml(dataset, de.xml());
			} else {
				filePath = ExportUtil.writeZipFile(dataset, headerJSON, StringUtils.isEmpty(exportName) ? "exportfile" : exportName);
			}
		}
		
		TlMExportlog export = new TlMExportlog();
		export.setExportPath(filePath);
		export.setTotal(new BigDecimal(total));
		return export;
	}
	
	/**
	 * 设置导出参数
	 * 
	 * @param pjp
	 * @param exportEntity
	 * @param headerJSON
	 * @throws Exception
	 */
	private void setExportParams(ProceedingJoinPoint pjp, ExportEntity exportEntity, String headerJSON) throws Exception {
		//设置headerJSON
		Method targetMethod = ((MethodSignature) pjp.getSignature()).getMethod();
		DataExport de = targetMethod.getAnnotation(DataExport.class);
		if (de != null && !StringUtils.isEmpty(de.xml())) {
			headerJSON = ExportUtil.getExportXmlConfig(de.xml()).getHeaderJSON();
		}
		
		String className = pjp.getTarget().getClass().getName();
		String methodName = pjp.getSignature().getName();
		Object[] args = pjp.getArgs();
		Class<?>[] parameterTypes = targetMethod.getParameterTypes();
		
		JSONObject newParams = new JSONObject();
		newParams.put("headerJSON", headerJSON);
		newParams.put("className", className);
		newParams.put("methodName", methodName);
		newParams.put("principal", Hex.encodeHexString(serializer.serialize(SecurityUtils.getSubject().getPrincipal())));
		if(args != null) {
			newParams.put("exportParams", Hex.encodeHexString(serializer.serialize(Arrays.asList(args))));
		}
		if(parameterTypes != null) {
			newParams.put("exportParamsClass", Hex.encodeHexString(serializer.serialize(parameterTypes)));
		}
		
		exportEntity.setParams(newParams.toString());
	}

	public ExecutorService getThreadPool() {
		if(threadPool == null){
			threadPool = Executors.newFixedThreadPool(getThreadPoolSize());
		}
		return threadPool;
	}

	public int getThreadPoolSize() {
		return threadPoolSize;
	}

	public void setThreadPoolSize(int threadPoolSize) {
		this.threadPoolSize = threadPoolSize;
	}

}
