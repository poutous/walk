package org.walkframework.restful.converter;

import java.io.IOException;
import java.lang.reflect.Type;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.NamedThreadLocal;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.jdbc.BadSqlGrammarException;
import org.walkframework.base.system.common.Common;
import org.walkframework.restful.constant.RspConstants;
import org.walkframework.restful.model.rsp.RspInfo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 自定义json转换器
 * 
 * @author shf675
 *
 */
public class CustomMappingJackson2HttpMessageConverter extends MappingJackson2HttpMessageConverter {

	protected final static ObjectMapper objectMapper = new ObjectMapper();
	
	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private static final ThreadLocal<Long> startTimeThreadLocal = new NamedThreadLocal<Long>("ThreadLocal StartTime");
	
	private static final ThreadLocal<RestfulLog> restfulLogThreadLocal = new NamedThreadLocal<RestfulLog>("ThreadLocal RestfulLog");
	
	@Autowired
	private HttpServletRequest request;
	
	@Override
	public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
		RestfulLog restfulLog = null;
		if (log.isInfoEnabled()) {
			restfulLog = new RestfulLog();
			restfulLog.setRequestURL(request.getRequestURI());
			restfulLog.setRequestHeaders(inputMessage.getHeaders());
			restfulLog.setSourceIP(Common.getInstance().getIpAddr(request));
			
			//设置开始时间
			startTimeThreadLocal.set(System.currentTimeMillis());
			
			//设置日志对象
			restfulLogThreadLocal.set(restfulLog);
		}
		
		Object object = null;
		try {
			object = super.read(type, contextClass, inputMessage);
			if(restfulLog != null){
				restfulLog.setRequestBody(object);
			}
		} catch (IOException e) {
			printErrorCallLog(e);
			throw e;
		} catch (HttpMessageNotReadableException e) {
			printErrorCallLog(e);
			throw e;
		} catch (Exception e) {
			printErrorCallLog(e);
			throw new RuntimeException(e);
		}
		return object;
	}
	
	@Override
	protected void writeInternal(Object object, Type type, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
		try {
			super.writeInternal(object, type, outputMessage);

			//打印返回信息
			if (log.isInfoEnabled()) {
				printCallLog(object);
			}
		} catch (Throwable e) {
			Object errorMessage = e.getMessage();
			//避免将sql抛到前台
			if (e instanceof BadSqlGrammarException) {
				errorMessage = e.getCause();
			}
			String errorMsg = RspConstants.RSP.get(RspConstants.INTERNAL_ERROR) + "：" + errorMessage;
			Object msg = getRspInfo(RspConstants.INTERNAL_ERROR, errorMsg);
			super.writeInternal(msg, type, outputMessage);

			log.error(errorMsg, e);
			
			//打印返回信息
			if (log.isInfoEnabled()) {
				printCallLog(msg);
			}
		}
	}
	
	/**
	 * 打印日志
	 * 
	 * @param object
	 */
	private void printCallLog(Object object){
		RestfulLog restfulLog = restfulLogThreadLocal.get();
		if(restfulLog != null){
			Long beginTime = startTimeThreadLocal.get();
			Double endTime = null;
			if(beginTime != null){
				endTime = ((double) (System.currentTimeMillis() - beginTime) / (double) 1000);
			}
			
			restfulLog.setResponseBody(object);
			try {
				log.info("<====> Call success, cost time: {}, call log: {}", endTime, objectMapper.writeValueAsString(restfulLog));
			} catch (JsonProcessingException e) {
				log.error(e.getMessage(), e);
			}
		}
	}
	
	/**
	 * 打印错误日志
	 * 
	 * @param e
	 */
	private void printErrorCallLog(Exception e){
		if (log.isInfoEnabled()) {
			Object errorMessage = e.getMessage();
			String errorMsg = RspConstants.RSP.get(RspConstants.OTHER_ERROR) + "：" + errorMessage;
			Object msg = getRspInfo(RspConstants.OTHER_ERROR, errorMsg);
			printCallLog(msg);
		}
	}
	
	/**
	 * 返回信息
	 * 
	 * @param respDesc
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("rawtypes")
	private RspInfo getRspInfo(Integer rspCode, String rspDesc) {
		RspInfo rspInfo = new RspInfo();
		rspInfo.setRspCode(rspCode);
		rspInfo.setRspDesc(rspDesc);
		return rspInfo;
	}

}
