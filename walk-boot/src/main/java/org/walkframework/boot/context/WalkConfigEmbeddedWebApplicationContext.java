package org.walkframework.boot.context;

import java.util.Collection;
import java.util.Map;

import org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletContextInitializerBeans;

/**
 * 重写 org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext
 * 
 * @author shf675
 *
 */
public class WalkConfigEmbeddedWebApplicationContext extends AnnotationConfigEmbeddedWebApplicationContext {
	
	/**
	 * 默认禁用filter自动注册
	 * 
	 * @return
	 */
	protected Collection<ServletContextInitializer> getServletContextInitializerBeans() {
		Collection<ServletContextInitializer> beans = new ServletContextInitializerBeans(getBeanFactory());
		
		Boolean enabled = Boolean.valueOf(getEnvironment().getProperty("spring.boot.enableautoregister", "true"));
		if(!enabled){
			for (ServletContextInitializer bean : beans) {
				if (bean instanceof FilterRegistrationBean) {
					FilterRegistrationBean filterBean = (FilterRegistrationBean) bean;
					//默认禁用filter自动注册
					filterBean.setEnabled(false);
					
					Map<String, String> param = filterBean.getInitParameters();
					if("true".equals(param.get("enabled"))){
						filterBean.setEnabled(true);
					}
				}
			}
		}
		return beans;
	}
}
