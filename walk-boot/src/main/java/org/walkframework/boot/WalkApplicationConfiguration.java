package org.walkframework.boot;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.EmbeddedServletContainerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ServerPropertiesAutoConfiguration;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ErrorPage;
import org.springframework.boot.web.servlet.ErrorPageRegistrar;
import org.springframework.boot.web.servlet.ErrorPageRegistry;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;
import org.walkframework.boot.config.ContextConfig;
import org.walkframework.boot.config.ErrorPageProperties;
import org.walkframework.boot.context.WalkConfigEmbeddedWebApplicationContext;
import org.walkframework.boot.filter.BootJspFilter;
import org.walkframework.boot.jsonp.JsonpCallbackFilter;
import org.walkframework.boot.reader.BootXmlBeanDefinitionReader;
import org.walkframework.boot.startup.WalkSpringApplication;

/**
 * spring boot方式启动工程
 * 
 * @author shf675
 * 
 */
@Configuration
@EnableConfigurationProperties( { ErrorPageProperties.class })
@Import( { EmbeddedServletContainerAutoConfiguration.class, ServerPropertiesAutoConfiguration.class })
@ImportResource(locations = { "${spring.boot.beans.location}" }, reader = BootXmlBeanDefinitionReader.class)
public class WalkApplicationConfiguration implements ServletContextInitializer {
	
	private static final Logger log = LoggerFactory.getLogger(WalkApplicationConfiguration.class);

	private static final String DEFAULT_DS_LOCATION = "classpath:boot-ds.xml";

	@Autowired
	private Environment environment;
	
	@Autowired
	private WebApplicationContext context;
	
	@Autowired
	private ErrorPageProperties errorPageProperties;
	
	@Autowired
	private DispatcherServlet dispatcherServlet;
	
	/**
	 * jar或直接运行BootRun方式启动工程入口
	 * 
	 * spring boot启动时调用
	 * 
	 * @param args
	 */
	public static void run(String[] args, Object... sources) {
		Set<String> arguments = new HashSet<String>(Arrays.asList(args));
		// 设定默认属性文件为app.properties
		//arguments.add("--spring.config.name=app");
		arguments.add("--spring.load.embedded=true");

		SpringApplication app = new WalkSpringApplication(sources);
		app.setApplicationContextClass(WalkConfigEmbeddedWebApplicationContext.class);
		ApplicationContext applicationContext = app.run(arguments.toArray(new String[arguments.size()]));
		
		//打印项目启动信息
		String serverPort = applicationContext.getEnvironment().getProperty("server.port");
		String serverContextPath = applicationContext.getEnvironment().getProperty("server.context-path");
		System.out.println("====================================================");
		System.out.println(String.format("Project is running at http://127.0.0.1:%s%s", serverPort, serverContextPath));
		System.out.println("====================================================");
	}
	
	/**
	 * tomcat个性化设置
	 * 
	 * @return
	 */
	@Bean
	@ConditionalOnProperty("spring.load.embedded")
	public TomcatEmbeddedServletContainerFactory tomcatEmbeddedServletContainerFactory() {
		// 自定义port、context-path
		TomcatEmbeddedServletContainerFactory container = new TomcatEmbeddedServletContainerFactory() {
			@Override
			protected void customizeConnector(Connector connector) {
				super.customizeConnector(connector);
				// 一些个性化设置
				connector.setUseBodyEncodingForURI(Boolean.valueOf(environment.getProperty("server.tomcat.use-body-encoding-for-uri", "false")));
			}

			@Override
			protected TomcatEmbeddedServletContainer getTomcatEmbeddedServletContainer(Tomcat tomcat) {
				// 开启命名服务。使用JNDI
				if ("true".equals(environment.getProperty("ds.enable-naming", "true"))) {
					tomcat.enableNaming();
				}
				return super.getTomcatEmbeddedServletContainer(tomcat);
			}

			@Override
			protected void postProcessContext(Context ctx) {
				super.postProcessContext(ctx);

				// 自定义配置
				processContextConfig(ctx);
			}
		};

		return container;
	}
	
	/**
	 * 自定义配置
	 * 
	 * @param ctx
	 */
	protected void processContextConfig(Context ctx) {
		// 从XML文件中加载JNDI数据源
		if ("true".equals(environment.getProperty("ds.enable-naming", "true"))) {
			String contextXml = environment.getProperty("ds.location");
			Resource resource = context.getResource(contextXml);
			if (!resource.exists()) {
				log.warn("The specified datasource[{}] file does not exist, using the default file [{}].", contextXml, DEFAULT_DS_LOCATION);
				resource = context.getResource(DEFAULT_DS_LOCATION);
			}
			try {
				new ContextConfig().processContextConfig(ctx, resource.getURL());
			} catch (Exception e) {
				log.error("process context file[{}] error!", contextXml, e);
			}
		}
	}
	
	/**
	 * 错误页面定义
	 * 
	 * @return
	 */
	@Bean
	public ErrorPageRegistrar errorPageRegistrarFactory(){
		return new ErrorPageRegistrar(){
			@Override
			public void registerErrorPages(ErrorPageRegistry registry) {
				Set<ErrorPage> errorPages = errorPageProperties.resolveErrorPages();
				if(!CollectionUtils.isEmpty(errorPages)){
					registry.addErrorPages(errorPages.toArray(new ErrorPage[errorPages.size()]));
				}
			}
		};
	}
	
	@Override
	public void onStartup(ServletContext container) throws ServletException {
		// Spring MVC配置
		if ("true".equals(environment.getProperty("spring.boot.mvc.load", "true"))) {
//			DispatcherServlet springMVC = new DispatcherServlet();
//			springMVC.setContextConfigLocation(environment.getProperty("spring.boot.mvc.location", "classpath:spring/spring-mvc.xml"));
			ServletRegistration.Dynamic springMVCRegistration = container.addServlet("springMVC", dispatcherServlet);
			springMVCRegistration.setLoadOnStartup(1);
			springMVCRegistration.addMapping("/");
		}

		// 过滤器1：不允许直接访问pages目录下的jsp 直接跳转到404页面
		if ("true".equals(environment.getProperty("spring.boot.jspfilter.load", "true"))) {
			FilterRegistration.Dynamic jspFilterRegistration = container.addFilter("jspFilter", new BootJspFilter());
			jspFilterRegistration.addMappingForUrlPatterns(null, true, "/pages/*");
		}

		// 过滤器2：解决post请求中文乱码。get请求乱码解决办法：如果是tomcat的话需在server.xml第一个Connector中加入URIEncoding="UTF-8"
		if ("true".equals(environment.getProperty("spring.boot.encodingfilter.load", "true"))) {
			FilterRegistration.Dynamic encodingFilterRegistration = container.addFilter("encodingFilter", new CharacterEncodingFilter("UTF-8", true));
			encodingFilterRegistration.addMappingForUrlPatterns(null, true, "/*");
		}

		// 过滤器3：shiro 安全过滤器
		if ("true".equals(environment.getProperty("spring.boot.shiro.load", "true"))) {
			DelegatingFilterProxy shiroFilter = new DelegatingFilterProxy();
			shiroFilter.setTargetFilterLifecycle(true);
			FilterRegistration.Dynamic shiroFilterRegistration = container.addFilter("shiroFilter", shiroFilter);
			shiroFilterRegistration.addMappingForUrlPatterns(null, true, "/*");
		}

		// fusioncharts导出图片servlet
		if ("true".equals(environment.getProperty("spring.boot.fusionchartsexporter.load", "true"))) {
			try {
				ServletRegistration.Dynamic fcexporterRegistration = container.addServlet("FCExporter", "com.fusioncharts.exporter.servlet.FCExporter");
				fcexporterRegistration.setLoadOnStartup(1);
				fcexporterRegistration.addMapping("/FCExporter");
			} catch (Exception e) {
				log.warn(e.getMessage(), e);
			}
		}
		// jsonp 过滤器
		if ("true".equals(environment.getProperty("spring.boot.jsonpfilter.load", "false"))) {
			FilterRegistration.Dynamic jsonpFilterRegistration = container.addFilter("JSONPFilter", new JsonpCallbackFilter());
			jsonpFilterRegistration.addMappingForUrlPatterns(null, true, "/*");
		}
	}

}