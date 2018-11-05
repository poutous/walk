package org.walkframework.base.system.interceptor;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.walkframework.base.system.common.Common;
import org.walkframework.base.system.common.Message;
import org.walkframework.base.system.factory.SingletonFactory;
import org.walkframework.data.bean.RespInfo;

import com.alibaba.fastjson.JSONArray;


/**
 * 所有拦截器继承此类
 *
 */
public class BaseInterceptor extends HandlerInterceptorAdapter {
	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	
	protected final static Common common = SingletonFactory.getInstance(Common.class);
	
	protected final static Message message = SingletonFactory.getInstance(Message.class);
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		return super.preHandle(request, response, handler);
	}
	
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		super.postHandle(request, response, handler, modelAndView);
	}
	
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		super.afterCompletion(request, response, handler, ex);
	}
	
	
	/**
	 * 向浏览器写返回信息
	 * 
	 * @param respCode
	 * @param respDesc
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	protected void writerRespInfo(HttpServletResponse response, String respCode, String respDesc) throws IOException {
		RespInfo respInfo = new RespInfo();
		respInfo.setRespCode(respCode);
		respInfo.setRespDesc(respDesc);

		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html;charset=utf-8");
		response.getWriter().write(JSONArray.toJSONString(respInfo));
	}
	
}
