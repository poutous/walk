package org.walkframework.batis.spring;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import name.pachler.nio.file.FileSystems;
import name.pachler.nio.file.Path;
import name.pachler.nio.file.Paths;
import name.pachler.nio.file.StandardWatchEventKind;
import name.pachler.nio.file.WatchEvent;
import name.pachler.nio.file.WatchKey;
import name.pachler.nio.file.WatchService;

import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.builder.xml.XMLMapperEntityResolver;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * @description: 扩展SqlSessionFactoryBean，支持Mybatis热部署
 * 
 * @author shf675
 */
public class HotSqlSessionFactoryBean extends SqlSessionFactoryBean {
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
	
	private Resource configLocation;

	//mybatis-config.xml中mappers节点
	private XNode mappersNode;
	
	//是否开启热部署
	private boolean isHotDeploy;

	//sql扫描路径
	private String sqlLocations;

	//热部署目录列表
	private Set<String> hotDeployLocations;

	public void setSqlLocations(String sqlLocations) throws IOException {
		this.sqlLocations = sqlLocations;

		super.setMapperLocations(findResource());
	}

	public void setHotDeployLocations(Set<String> hotDeployLocations) {
		this.hotDeployLocations = hotDeployLocations;
	}

	/**
	 * 创建SqlSessionFactory
	 * 
	 * @throws IOException
	 */
	protected SqlSessionFactory buildSqlSessionFactory() throws IOException {
		SqlSessionFactory sqlSessionFactory = super.buildSqlSessionFactory();
		Properties properties = sqlSessionFactory.getConfiguration().getVariables();
		if(properties == null){
			sqlSessionFactory.getConfiguration().setVariables(new Properties());
			properties = sqlSessionFactory.getConfiguration().getVariables();
		}
		properties.setProperty("isHotDeploy", String.valueOf(isHotDeploy));

		// 设置了监控目录，启动mybatis热部署
		if (isHotDeploy && this.hotDeployLocations != null && this.hotDeployLocations.size() > 0) {
			startReloadXML(sqlSessionFactory.getConfiguration());
		}
		return sqlSessionFactory;
	}

	/**
	 * 启动xml重新加载程序
	 * 
	 * @param configuration
	 */
	private void startReloadXML(final Configuration configuration) {
		try {
			//获取mybatis-config.xml中mappers节点
			mappersNode = new XPathParser(configLocation.getInputStream(), true, null, new XMLMapperEntityResolver()).evalNode("/configuration").evalNode("mappers");
			
			//监控目录
			for (String hotDeployLocation : hotDeployLocations) {
				String monitorPath = this.getClass().getClassLoader().getResource("").getPath() + hotDeployLocation;
				if(new File(monitorPath).isDirectory()){
					new Thread(new Monitor(Paths.get(monitorPath), new AtomicBoolean(false), configuration)).start();
					log.debug("Mybatis hot deployment process. Monitor root directory：{}", monitorPath);
				} else {
					log.warn("{} is not a directory", monitorPath);
				}
			}

		} catch (Exception e) {
			log.error("Error starting Mybatis hot deployment", e);
		}
	}

	/**
	 * 搜索xml文件
	 * 
	 * @return
	 * @throws IOException
	 */
	private Resource[] findResource() throws IOException {
		return resourcePatternResolver.getResources(this.sqlLocations);
	}

	@SuppressWarnings("unchecked")
	private class Monitor implements Runnable {
		private AtomicBoolean stop = null;
		private Configuration configuration = null;

		WatchService watcher = null;
		WatchKey watckKey = null;

		public Monitor(Path path, AtomicBoolean stop, Configuration configuration) {
			this.stop = stop;
			this.configuration = configuration;

			try {
				watcher = FileSystems.getDefault().newWatchService();

				// 监控新建、修改、删除的文件
				path.register(watcher, StandardWatchEventKind.ENTRY_CREATE, StandardWatchEventKind.ENTRY_MODIFY, StandardWatchEventKind.ENTRY_DELETE);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}

		@SuppressWarnings( { "unchecked", "static-access" })
		@Override
		public void run() {
			try {
				watckKey = watcher.take();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			while (!stop.get()) {
				try {
					List<WatchEvent<?>> nevents = watckKey.pollEvents();
					if (nevents != null && nevents.size() > 0) {
						reloadXML(configuration);
					}
					Thread.currentThread().sleep(500);
				} catch (Exception ex) {
					log.error(ex.getMessage(), ex);
				}
			}
		}

		public void stop() {
			stop.compareAndSet(false, true);
		}

		/**
		 * 重新加载所有xml
		 * 
		 * @param configuration
		 * @throws Exception
		 */
		public void reloadXML(Configuration configuration) throws Exception {
			log.debug("File or directory is modified, reload all XML files to start...");
			long beginTime = System.currentTimeMillis();
			removeConfig(configuration);
			Resource[] resources = findResource();
			for (Resource resource : resources) {
				try {
					log.debug("加载{}", resource);
					XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(resource.getInputStream(), configuration, resource.toString(), configuration.getSqlFragments());
					xmlMapperBuilder.parse();
				} finally {
					ErrorContext.instance().reset();
				}
			}

			//处理mybatis-config.xml中mappers节点
			mapperElement(configuration);

			double s = ((double) (System.currentTimeMillis() - beginTime) / (double) 1000);
			log.debug("XML file reload. This reload time consuming {}s. Total load file {}.", s, resources.length);
		}

		/**
		 * 移除原配置信息
		 * 
		 * @param configuration
		 * @throws Exception
		 */
		private void removeConfig(Configuration configuration) throws Exception {
			Class<?> classConfig = configuration.getClass();
			clearMap(classConfig, configuration, "mappedStatements");
			clearMap(classConfig, configuration, "caches");
			clearMap(classConfig, configuration, "resultMaps");
			clearMap(classConfig, configuration, "parameterMaps");
			clearMap(classConfig, configuration, "keyGenerators");
			clearMap(classConfig, configuration, "sqlFragments");
			clearSet(classConfig, configuration, "loadedResources");
		}

		private void clearMap(Class<?> classConfig, Configuration configuration, String fieldName) throws Exception {
			Field field = classConfig.getDeclaredField(fieldName);
			field.setAccessible(true);
			((Map) field.get(configuration)).clear();
		}

		private void clearSet(Class<?> classConfig, Configuration configuration, String fieldName) throws Exception {
			Field field = classConfig.getDeclaredField(fieldName);
			field.setAccessible(true);
			((Set) field.get(configuration)).clear();
		}
	}

	public void setIsHotDeploy(boolean isHotDeploy) {
		this.isHotDeploy = isHotDeploy;
	}

	public void setConfigLocation(Resource configLocation) {
		this.configLocation = configLocation;
		super.setConfigLocation(configLocation);
	}

	/**
	 * 处理mybatis-config.xml中mappers节点
	 * 
	 * @param configuration
	 * @throws Exception
	 */
	private void mapperElement(Configuration configuration) throws Exception {
		if (mappersNode != null) {
			for (XNode child : mappersNode.getChildren()) {
				if ("package".equals(child.getName())) {
					String mapperPackage = child.getStringAttribute("name");
					configuration.addMappers(mapperPackage);
				} else {
					String resource = child.getStringAttribute("resource");
					String url = child.getStringAttribute("url");
					String mapperClass = child.getStringAttribute("class");
					if (resource != null && url == null && mapperClass == null) {
						ErrorContext.instance().resource(resource);
						InputStream inputStream = Resources.getResourceAsStream(resource);
						XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, resource, configuration.getSqlFragments());
						mapperParser.parse();
					} else if (resource == null && url != null && mapperClass == null) {
						ErrorContext.instance().resource(url);
						InputStream inputStream = Resources.getUrlAsStream(url);
						XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, url, configuration.getSqlFragments());
						mapperParser.parse();
					} else if (resource == null && url == null && mapperClass != null) {
						Class<?> mapperInterface = Resources.classForName(mapperClass);
						configuration.addMapper(mapperInterface);
					} else {
						throw new BuilderException("A mapper element may only specify a url, resource or class, but not more than one.");
					}
				}
			}
		}
	}
}
