package org.walkframework.base.system.listener;

import javax.servlet.ServletContextEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ContextLoaderListener;

/**
 * spring容器基类
 * 
 * 扩展：启动类增强器
 * 
 * @author shf675
 *
 */
public class WalkContextLoaderListener extends ContextLoaderListener {

	private static final Logger log = LoggerFactory.getLogger(WalkContextLoaderListener.class);

	public static final String ENHANCE_CONFIG_LOCATION_PARAM = "enhanceConfigLocation";
	
	@Override
	public void contextInitialized(ServletContextEvent event) {
		//1、加载类增强器
		enhanceInitialized(event);

		//2、spring容器启动
		super.contextInitialized(event);
	}

	/**
	 * 加载类增强器
	 * 
	 * @param event
	 */
	public void enhanceInitialized(ServletContextEvent event) {
		String enhanceConfigLocation = event.getServletContext().getInitParameter(ENHANCE_CONFIG_LOCATION_PARAM);
		try {
			if(!StringUtils.isEmpty(enhanceConfigLocation)){
				new ClassPathXmlApplicationContext(enhanceConfigLocation);
			}
		} catch (Exception e) {
			log.error("class enhancer loading failed...", e);
		}
	}
}
