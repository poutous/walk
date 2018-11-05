package org.walkframework.restful.config;

import io.swagger.converter.ModelConverters;
import io.swagger.validator.BeanValidator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import com.google.common.base.Predicate;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/** 
 * 
 * Restful API 访问路径: http://IP:port/{context-path}/swagger-ui.html
 * 
 * @author shf675
 * 
 */
@EnableSwagger2
public class SwaggerConfig extends WebMvcConfigurationSupport {
	
	/**服务标题*/
	private String title = "service list";
	
	/**服务版本*/
	private String version = "1.0";
	
	/**正则匹配路径*/
	private String pathRegex;
	
	@Bean
	public Docket createRestApi() {
		ModelConverters.getInstance().addConverter(new BeanValidator());
		
		Predicate<String> paths = PathSelectors.any();
		if(StringUtils.isNotEmpty(getPathRegex())){
			paths = PathSelectors.regex(getPathRegex());
		}
		return new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo()).select().paths(paths).build();
	}

	private ApiInfo apiInfo() {
		return new ApiInfoBuilder().title(getTitle() + getVersion()).version(getVersion()).build();
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getPathRegex() {
		return pathRegex;
	}

	public void setPathRegex(String pathRegex) {
		this.pathRegex = pathRegex;
	}
}