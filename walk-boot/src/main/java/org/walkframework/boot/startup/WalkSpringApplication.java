package org.walkframework.boot.startup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.StringUtils;

/**
 * 自定义扩展
 * 
 * @author shf675
 *
 */
public class WalkSpringApplication extends SpringApplication {
	
	private static final Logger log = LoggerFactory.getLogger(WalkSpringApplication.class);
	
	public WalkSpringApplication(Object... sources) {
		super(sources);
	}
	
	@Override
	protected void load(ApplicationContext context, Object[] sources) {
		
		//类增强器初始化
		String enhanceConfigLocation = context.getEnvironment().getProperty("spring.boot.enhance.location");
		enhanceInitialized(enhanceConfigLocation);
		
		super.load(context, sources);
	}
	
	/**
	 * 类增强器初始化
	 * 
	 * @param event
	 */
	public void enhanceInitialized(String enhanceConfigLocation) {
		try {
			if (!StringUtils.isEmpty(enhanceConfigLocation)) {
				new ClassPathXmlApplicationContext(enhanceConfigLocation);
			}
		} catch (Exception e) {
			log.error("class enhancer loading failed...", e);
		}
	}
}
