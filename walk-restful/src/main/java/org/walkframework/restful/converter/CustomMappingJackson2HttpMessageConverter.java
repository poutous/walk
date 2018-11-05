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

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 自定义json转换器
 * 
 * @author shf675
 *
 */
public class CustomMappingJackson2HttpMessageConverter extends MappingJackson2HttpMessageConverter {

	private final static ObjectMapper objectMapper = new ObjectMapper();
	
	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private static final ThreadLocal<Long> startTimeThreadLocal = new NamedThreadLocal<Long>("ThreadLocal StartTime");
	
	@Autowired
	private HttpServletRequest request;
	
	@Override
	public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
		Object object = super.read(type, contextClass, inputMessage);
		
		//打印请求信息
		if (log.isInfoEnabled()) {
			try {
				//设置开始时间
				startTimeThreadLocal.set(System.currentTimeMillis());
				log.info("*********** Request source IP: {}", Common.getInstance().getIpAddr(request));
				log.info("*********** Request Headers: {}", objectMapper.writeValueAsString(inputMessage.getHeaders()));
				log.info("*********** Request URL: {}", request.getRequestURI());
				log.info("*********** Request Body: {}", objectMapper.writeValueAsString(object));
			} catch (Throwable thr) {
				log.error(thr.getMessage(), thr);
			}
		}
		return object;
	}

	@Override
	protected void writeInternal(Object object, Type type, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
		try {
			super.writeInternal(object, type, outputMessage);

			//打印返回信息
			if (log.isInfoEnabled()) {
				log.info("*********** Response Body: {}", objectMapper.writeValueAsString(object));
				log.info("*********** Response Headers: {}", objectMapper.writeValueAsString(outputMessage.getHeaders()));
				Long beginTime = startTimeThreadLocal.get();
				if(beginTime != null){
					log.info("*********** Cost time: {}", ((double) (System.currentTimeMillis() - beginTime) / (double) 1000));
				}
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
				try {
					log.info("*********** Response Body: {}", objectMapper.writeValueAsString(msg));
					log.info("*********** Response Headers: {}", objectMapper.writeValueAsString(outputMessage.getHeaders()));
					Long beginTime = startTimeThreadLocal.get();
					if(beginTime != null){
						log.info("*********** Cost time: {}", ((double) (System.currentTimeMillis() - beginTime) / (double) 1000));
					}
				} catch (Throwable thr) {
					log.error(thr.getMessage(), thr);
				}
			}
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
