package org.walkframework.base.system.interceptor;

import java.io.File;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.walkframework.base.mvc.dao.BaseSqlSessionDao;
import org.walkframework.base.mvc.entity.TlMExportlog;
import org.walkframework.base.system.annotation.DataExport;
import org.walkframework.base.system.common.Common;
import org.walkframework.base.system.constant.CommonConstants;
import org.walkframework.base.system.factory.SingletonFactory;
import org.walkframework.base.tools.spring.SpringContextHolder;
import org.walkframework.base.tools.spring.SpringPropertyHolder;
import org.walkframework.base.tools.utils.ExportUtil;
import org.walkframework.batis.dao.SqlSessionDao;
import org.walkframework.batis.dialect.OracleDialect;
import org.walkframework.data.bean.PageData;

import com.alibaba.fastjson.JSON;

/**
 * spring校验结果拦截器
 * 
 * @author shf675
 *
 */
public class AsynExportInterceptor {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final static Common common = SingletonFactory.getInstance(Common.class);

	/**
	 * 线程池默认大小100
	 */
	private int DEFAULT_POOL_SIZE = 100;
	
	/**
	 * 线程池大小
	 */
	private int threadPoolSize = DEFAULT_POOL_SIZE;
	
	/**
	 * 线程池
	 */
	private ExecutorService threadPool;
	
	@Autowired
	HttpServletRequest request;

	@Autowired
	HttpServletResponse response;

	private SqlSessionDao dao;

	/**
	 * 环绕方法
	 * 
	 * @param pjp
	 * @param bindingResult
	 * @return
	 * @throws Throwable
	 */
	public Object doAround(final ProceedingJoinPoint pjp) throws Throwable {
		if (CommonConstants.ACTION_TYPE_ASYN_EXPORT.equals(request.getParameter(CommonConstants.ACTION_TYPE))) {
			//导出文件名称
			final String exportName = request.getParameter(CommonConstants.ASYN_EXPORT_NAME);
			
			//导出表头
			final String headerJSON = request.getParameter(CommonConstants.HEADER_JSON_NAME);

			//记录入库
			final String exportId = createExport();

			//开启新的线程进行异步导出
			getThreadPool().execute(new Runnable() {
				@Override
				public void run() {
					try {
						//导出
						TlMExportlog elog = doExport(pjp, headerJSON, exportName);
						elog.setLogId(exportId);
						
						//记录成功信息
						recordSuccess(elog);
					} catch (Throwable e) {
						log.error("导出失败[" + exportId + "]", e);

						//记录失败信息
						recordFailure(exportId, e.getMessage());
					}
				}
			});

			//返回导出流水
			response.setHeader(CommonConstants.ASYN_EXPORT_ID, exportId);

			//异步导出返回null
			return null;
		}
		return pjp.proceed();
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
	 * 创建导出任务
	 * 
	 * @param exportName
	 * @throws Exception 
	 */
	private String createExport() throws Exception {
		String exportId = getExportId();
		TlMExportlog export = new TlMExportlog();
		export.setLogId(exportId);
		export.setExportName(request.getParameter(CommonConstants.ASYN_EXPORT_NAME));
		export.setExportMode(CommonConstants.EXPORT_MODE_ASYN);
		export.setExportState(CommonConstants.EXPORT_STATE_EXECUTING);
		
		export.setReqUri(request.getRequestURI());
		export.setParams(JSON.toJSONString(common.getInParam(request)));
		export.setOperateIp(common.getIpAddr(request));
		export.setCreateStaff((String)common.getValueByFieldName(SecurityUtils.getSubject().getPrincipal(), "userId"));
		export.setCreateTime(new Date());
		dao().insert(export);
		
		return exportId;
	}

	/**
	 * 记录成功信息
	 * 
	 * @param succ
	 */
	private void recordSuccess(TlMExportlog succ) {
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
	private void recordFailure(String exportId, String errMsg) {
		TlMExportlog err = new TlMExportlog();
		err.setLogId(exportId).asCondition();
		err.setExportState(CommonConstants.EXPORT_STATE_FAILURE);
		err.setFinishTime(new Date());
		err.setRemark(errMsg);
		dao().update(err);
	}

	private String getExportId() {
		String fileId = "";
		if (dao().getDialect() instanceof OracleDialect) {
			//oracle:序列+14位随机数
			fileId = ((BaseSqlSessionDao) dao()).getSequenceL16("SEQ_FILE_ID") + common.getRandomString(14);
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

	public SqlSessionDao dao() {
		if (dao == null) {
			dao = SpringContextHolder.getBean(SpringPropertyHolder.getContextProperty("walkbatis.defaultSqlSessionDaoName", "sqlSessionDao"), SqlSessionDao.class);
		}
		return dao;
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
