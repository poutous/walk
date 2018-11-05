package org.walkframework.base.tools.spring;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.util.PropertyPlaceholderHelper;
import org.walkframework.base.tools.utils.EncryptUtil;

/**
 * @ClassName: SpringPropertyHolder
 * @Description: 实例化在applicationcontext中，用来获取配置在Spring中的Property值
 */
public class SpringPropertyHolder extends PropertyPlaceholderConfigurer{
	
	private static final Logger log = LoggerFactory.getLogger(SpringPropertyHolder.class);
	
	private String key;
	
	private ApplicationContext applicationContext;

	/**
	 * @Fields ctxPropertiesMap : 将Properties保存在静态Map中
	 */
	private static Map<String, String> ctxPropertiesMap = new HashMap<String, String>();

	@Override
	protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, Properties props) throws BeansException {
		tryGetApplicationContextFromBootEnvironment();
		
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
	 * 尝试从spring boot启动环境中获取ApplicationContext对象
	 */
	private void tryGetApplicationContextFromBootEnvironment() {
		try {
			Class<?> clazz = Class.forName("org.walkframework.boot.WalkApplicationConfiguration");
			Method method = clazz.getMethod("getApplicationContext");
			applicationContext = (ApplicationContext) method.invoke(null);
		} catch (Exception e) {
		}
	}
	
	/**
	 * 兼容spring boot方式启动外部指定参数优先启动
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	private String getProperty(String key, String defaultValue) {
		if(applicationContext != null) {
			return applicationContext.getEnvironment().getProperty(key, defaultValue);
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
		return ctxPropertiesMap.get(name) == null ? defVal : ctxPropertiesMap.get(name);
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
}
