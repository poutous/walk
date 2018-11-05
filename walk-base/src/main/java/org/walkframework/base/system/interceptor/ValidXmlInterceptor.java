package org.walkframework.base.system.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.walkframework.base.system.annotation.ValidXml;
import org.walkframework.base.tools.utils.ValidateUtil;
import org.walkframework.data.util.IData;

/**
 * 校验器拦截
 *
 */
public class ValidXmlInterceptor extends BaseInterceptor {
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		if(handler instanceof HandlerMethod){
	    	HandlerMethod method = (HandlerMethod)handler;
	    	
	    	//表单校验
	    	ValidXml validXml = method.getMethodAnnotation(ValidXml.class);
	    	if(validXml != null){
	    		IData<String, Object> param = common.getInParam(request);
	    		param.put(ValidateUtil.HANDLER_METHOD, method.getMethod().getName());
	    		String errors = ValidateUtil.validateForm(validXml.value(), param);
	    		if(!StringUtils.isEmpty(errors)){
	    			common.error(errors);
	    			return false;
	    		}
	    	}
	    }
	    return true;
	}
}
