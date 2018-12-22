package org.walkframework.base.tools.spring;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.util.PropertyPlaceholderHelper;
import org.walkframework.base.tools.utils.EncryptUtil;

/**
 * @ClassName: SpringPropertyHolder
 * @Description: 实例化在applicationcontext中，用来获取配置在Spring中的Property值
 */
public class SpringPropertyHolder extends PropertyPlaceholderConfigurer implements ApplicationContextAware{
	
	private static final Logger log = LoggerFactory.getLogger(SpringPropertyHolder.class);
	
	private String key;
	
	private static Environment environment;
	
	/**
	 * @Fields ctxPropertiesMap : 将Properties保存在静态Map中
	 */
	private static Map<String, String> ctxPropertiesMap = new HashMap<String, String>();

	@Override
	protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, Properties props) throws BeansException {
		PropertyPlaceholderHelper helper = new PropertyPlaceholderHelper(placeholderPrefix, placeholderSuffix, valueSeparator, ignoreUnresolvablePlaceholders);
		for (Object key : props.keySet()) {
			String keyStr = key.toString();
			String value = props.getProperty(keyStr);
			if (value != null && value.startsWith("{DES}")) {
				try {
					value = EncryptUtil.decryptByDES(value.substring(5).trim(), getKey());
				} catch (Exception e) {
					log.error("Password decryption failed, please check" + keyStr + "Configuration is correct.", e);
				}
			}
			value = helper.replacePlaceholders(getProperty(keyStr, value), props);
			
			ctxPropertiesMap.put(keyStr, value);
			props.setProperty(keyStr, value);
		}
		super.processProperties(beanFactoryToProcess, props);
	}
	
	/**
	 * getProperty
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	private static String getProperty(String key, String defaultValue) {
		if(environment != null) {
			return environment.getProperty(key, defaultValue);
		}
		return defaultValue;
	}
	
	/**
	 * @Title: setContextProperty
	 * @Description: 设置Spring从上下文中的properties文件中获取到的属性
	 * @param key 属性名称
	 * @param value 值
	 */
	public static void setContextProperty(String key, String value) {
		ctxPropertiesMap.put(key, value);
	}

	/**
	 * @Title: getContextProperty
	 * @Description: 获取Spring上下文中的properties文件中的属性
	 * @param name 属性名称
	 * @return 属性值
	 */
	public static String getContextProperty(String name) {
		return getContextProperty(name, null);
	}

	/**
	 * @Title: getContextProperty
	 * @Description: 获取Spring上下文中的properties文件中的属性
	 * @param name 属性名称
	 * @param defVal 默认值
	 * @return 属性值
	 */
	public static String getContextProperty(String name, String defVal) {
		return ctxPropertiesMap.get(name) == null ? getProperty(name, defVal) : ctxPropertiesMap.get(name);
	}

	public String getKey() {
		return key == null ? EncryptUtil.SECURITY_KEY:key;
	}

	public void setKey(String key) {
		this.key = key;
	}
	
	public static Map<String, String> getProperties() {
		return ctxPropertiesMap;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		environment = applicationContext.getEnvironment();
	}
}
