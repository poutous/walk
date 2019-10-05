package org.walkframework.base.tools.spring;

/**
 * 属性工具类
 * 
 * @author shf675
 *
 */
public class SpringPropertyHolder {
	
	public static String getContextProperty(String name) {
		return getContextProperty(name, null);
	}

	public static String getContextProperty(String name, String defVal) {
		return SpringContextHolder.getApplicationContext().getEnvironment().getProperty(name, defVal);
	}
}
