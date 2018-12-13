package org.walkframework.boot.startup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

/**
 * SpringBootServletInitializer扩展
 * 
 * war包方式部署时使用
 * 
 * @author shf675
 *
 */
public abstract class WalkSpringBootServletInitializer extends SpringBootServletInitializer {
	
	@Override
	protected SpringApplicationBuilder createSpringApplicationBuilder() {
		return new SpringApplicationBuilder() {
			@Override
			protected SpringApplication createSpringApplication(Object... sources) {
				return new WalkSpringApplication();
			}
		};
	}
}
