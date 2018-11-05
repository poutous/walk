package org.walkframework.base.system.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.web.servlet.DispatcherServlet;

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
	
	private static final Logger log = LoggerFactory.getLogger(WalkDispatcherServlet.class);
	
	private final String PROPERTY_FILE = "app.properties";

	/**
	 * 预先加载属性文件，以便import标签使用
	 */
	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		try {
			applicationContext.getEnvironment().getPropertySources().addFirst(new ResourcePropertySource("classpath:" + PROPERTY_FILE));
		} catch (Exception e) {
			log.error("{} is not exists", PROPERTY_FILE);
		}
	}
}
