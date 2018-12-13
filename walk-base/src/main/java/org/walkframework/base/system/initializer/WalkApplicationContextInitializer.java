package org.walkframework.base.system.initializer;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * spring容器初始化
 * 
 * @author shf675
 *
 */
public class WalkApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>  {

	/**
	 * 预先加载属性文件，以便import标签使用
	 */
	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		SpringImportLableInitializer.loadProperties(applicationContext);
	}
}
