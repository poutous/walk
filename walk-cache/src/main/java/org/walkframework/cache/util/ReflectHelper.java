package org.walkframework.cache.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;


/**
 * @author shf675
 * 
 *	反射工具
 */
public abstract class ReflectHelper {
	private static final Logger log = LoggerFactory.getLogger(ReflectHelper.class);
	
	/**
	 * 反射执行方法
	 * 
	 * @param target
	 * @param methodName
	 * @param params
	 * @param paramsClass
	 * @return
	 */
	public static Object invokeMethod(Object target, Class<?> declaredClazz, String methodName, Object[] params, Class<?>[] paramsClass) {
		Object object = null;
		try {
			Method method = ReflectionUtils.findMethod(declaredClazz, methodName, paramsClass);
			method.setAccessible(true);
			object = method.invoke(target, params);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return object;
	}
	
	/**
	 * 获取属性值属性值
	 * 
	 * @param target
	 * @param propertyName
	 * @return
	 * @throws Exception
	 */
	public static Object getFieldValue(Object target, String propertyName){
		if ((target == null) || (propertyName == null) || ("".equals(propertyName))) {
			return null;
		}
		try {
			Field field = ReflectionUtils.findField(target.getClass(), propertyName);
			field.setAccessible(true);
			return field.get(target);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}
}
