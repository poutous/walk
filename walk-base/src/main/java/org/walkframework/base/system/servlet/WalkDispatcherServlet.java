package org.walkframework.base.system.servlet;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.walkframework.base.system.initializer.SpringImportLableInitializer;

/**
 * spring mvc 基类
 * 
 * 待扩展
 * 
 * @author shf675
 * 
 */
@SuppressWarnings("serial")
public class WalkDispatcherServlet extends DispatcherServlet implements ApplicationContextInitializer<ConfigurableApplicationContext> {

	/**
	 * 预先加载属性文件，以便import标签使用
	 */
	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		SpringImportLableInitializer.loadProperties(applicationContext);
	}
}
