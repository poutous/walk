package org.walkframework.base.system.bind;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.walkframework.base.tools.utils.ReflectionUtils;


public class RequestMappingHandlerAdapterAdjust implements InitializingBean{
	private RequestMappingHandlerAdapter requestMappingHandlerAdapter;
	
	public void setRequestMappingHandlerAdapter(RequestMappingHandlerAdapter requestMappingHandlerAdapter) {
		this.requestMappingHandlerAdapter = requestMappingHandlerAdapter;
	}


	@SuppressWarnings("unchecked")
	@Override
	public void afterPropertiesSet() throws Exception {
		List<HandlerMethodArgumentResolver> allArgumentResolvers = new ArrayList<HandlerMethodArgumentResolver>();
		
		List<HandlerMethodArgumentResolver> frontArgumentResolvers = this.requestMappingHandlerAdapter.getArgumentResolvers();
		List<HandlerMethodArgumentResolver> defaultArgumentResolvers = (List<HandlerMethodArgumentResolver>) ReflectionUtils.invoke(this.requestMappingHandlerAdapter, "getDefaultArgumentResolvers", null, null);
		
		//1、将先要执行的参数解决器放置最前
		allArgumentResolvers.addAll(frontArgumentResolvers);
		
		//2、添加默认的参数解决器
		allArgumentResolvers.addAll(defaultArgumentResolvers);
		
		//3、将调整后的解决器通过反射设置到原适配器中
		this.requestMappingHandlerAdapter.setArgumentResolvers(allArgumentResolvers);
	}

}
