package org.walkframework.base.system.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.method.HandlerMethod;
import org.walkframework.base.system.annotation.DataExport;
import org.walkframework.base.system.constant.CommonConstants;

/**
 * 自定义导出拦截器
 *
 */
public class DataExportInterceptor extends BaseInterceptor {
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		if(handler instanceof HandlerMethod){
	    	HandlerMethod method = (HandlerMethod)handler;
	    	
	    	String actionType = request.getParameter(CommonConstants.ACTION_TYPE);
	    	DataExport dataExport = method.getMethodAnnotation(DataExport.class);
	    	if(dataExport != null && CommonConstants.ACTION_TYPE_EXPORT.equals(actionType)){
	    		request.setAttribute(CommonConstants.EXPORT_XML_NAME, dataExport.xml());
	    	}
	    }
	    return true;
	}
}
