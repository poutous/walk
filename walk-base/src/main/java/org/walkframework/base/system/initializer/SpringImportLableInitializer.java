package org.walkframework.base.system.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePropertySource;

/**
 * spring import标签初始化，以便使用属性文件中配置
 * 
 * @author shf675
 *
 */
public class SpringImportLableInitializer {
	private static final Logger log = LoggerFactory.getLogger(SpringImportLableInitializer.class);
	
	private static final String PROPERTY_FILE_APPLICATION = "application.properties";
	
	private static final String PROPERTY_FILE_APP = "app.properties";
	
	/**
	 * 加载属性文件
	 * 
	 * @param applicationContext
	 */
	public static void loadProperties(ConfigurableApplicationContext applicationContext) {
		String propertyFile = PROPERTY_FILE_APPLICATION;
		try {
			Resource resource = applicationContext.getResource(propertyFile);
			if(!resource.exists()){
				propertyFile = PROPERTY_FILE_APP;
			}
			applicationContext.getEnvironment().getPropertySources().addFirst(new ResourcePropertySource("classpath:" + propertyFile));
		} catch (Exception e) {
			log.error("{} is not exists", propertyFile);
		}
	}
}
