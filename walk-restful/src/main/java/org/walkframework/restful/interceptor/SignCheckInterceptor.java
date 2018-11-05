package org.walkframework.restful.interceptor;

import java.io.IOException;
import java.lang.reflect.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;
import org.walkframework.restful.model.req.ReqInfo;

/**
 * 签名校验拦截器
 * 
 * 已废弃不用
 * 
 */
//@ControllerAdvice
@Deprecated
public class SignCheckInterceptor implements RequestBodyAdvice {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	
//	private SignChecker signChecker = new SignChecker();
//	
//	@Resource(name = "${cacheManagerName}")
//	private ICacheManager cacheManager;
//
//	@Resource(name = "${walkbatis.defaultSqlSessionDaoName}")
//	private BaseSqlSessionDao dao;

	@Override
	public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
		return ReqInfo.class.isAssignableFrom(methodParameter.getParameterType());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
//		// 签名校验总开关
//		String signSwitch = SpringPropertyHolder.getContextProperty("validate.sign", "false");
//		if ("false".equals(signSwitch)) {
//			return body;
//		}
//
//		ReqInfo reqInfo = (ReqInfo) body;
//		MetaObject meta = SystemMetaObject.forObject(reqInfo.getReqHead());
//
//		// 先测试是否有签名校验相关参数
//		try {
//			meta.getValue("appId");
//			meta.getValue("timestamp");
//			meta.getValue("sign");
//		} catch (Exception e) {
//			e.printStackTrace();
//			log.warn("签名校验总开关已开启(validate.sign参数)，但请求报文无签名相关参数！");
//			return body;
//		}
//
//		final String appId = (String) meta.getValue("appId");
//		String timestamp = (String) meta.getValue("timestamp");
//		final String sign = (String) meta.getValue("sign");
//		
//		//开始校验
//		signChecker.setCacheManager(cacheManager);
//		signChecker.setDao(dao);
//		signChecker.check(appId, timestamp, sign);
		return body;
	}

	@Override
	public Object handleEmptyBody(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
		return body;
	}

	@Override
	public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
		return inputMessage;
	}
}
