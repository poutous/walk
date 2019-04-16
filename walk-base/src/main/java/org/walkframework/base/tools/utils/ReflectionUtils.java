package org.walkframework.base.tools.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 反射工具类
 *
 */
public class ReflectionUtils {
	
	/**
	 * 设置属性值
	 * 无节操版
	 * @param target
	 * @param propertyName
	 * @param propertyValue
	 * @throws Exception
	 */
	public static void setFieldValue(Object target, String propertyName, Object propertyValue) throws Exception {
		if ((target == null) || (propertyName == null) || ("".equals(propertyName))) {
			return;
		}
		Field field = getDeclaredField(target, propertyName);
		field.setAccessible(true);
		field.set(target, propertyValue);
	}
	
	/**
	 * 获取属性值
	 * @param target
	 * @param property
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static Object getFieldValue(Object target, String property) throws Exception {
		if ((target == null) || (property == null) || ("".equals(property))) {
			return null;
		}
		if(target instanceof Map) {
			return ((Map)target).get(property)==null?"0":((Map)target).get(property).toString();
		}
		Class clazz = target.getClass();
		String getterName = getGetterName(property);
		Method method = clazz.getDeclaredMethod(getterName, new Class[0]);
		return method.invoke(target, new Object[0]);
	}
	
	/**
	 * 执行方法
	 * 
	 * @param target
	 * @param method_name
	 * @param params
	 * @param types
	 * @return
	 * @throws Exception
	 */
	public static Object invoke(Object target, String methodName, Object[] params, Class<?>[] paramsClass){
		if ((target == null) || (methodName == null) || ("".equals(methodName))) {
			return null;
		}
		Method method = getDeclaredMethod(target, methodName, paramsClass);
		method.setAccessible(true);
		Object object = null;
		try {
			object = method.invoke(target, params);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return object;
	}
	
	 /** 
     * 获取对象的属性，包括父类的
     * @param object : 对象 
     * @param fieldName : 属性名 
	 * @throws NoSuchFieldException 
	 * @throws SecurityException 
     */  
    public static Field getDeclaredField(Object object, String fieldName){
    	Class<?> clazz = object.getClass();
        Field field = null;
		try {
			field = clazz.getDeclaredField(fieldName);
		} catch (Exception e1) {
			for(; clazz != Object.class; clazz = clazz.getSuperclass()) {
				try {
					return clazz.getDeclaredField(fieldName);
				} catch (Exception e) {
				}
			}
		}
        return field;
    }
    
    /** 
     * 获取对象的方法，包括父类的
     * @param object : 对象 
     * @param methodName : 方法名
     * @throws NoSuchMethodException 
     * @throws SecurityException 
     */  
    public static Method getDeclaredMethod(Object object, String methodName, Class<?>[] paramsClass){
    	Class<?> clazz = object.getClass() ;
    	Method method = null;
		try {
			method = clazz.getDeclaredMethod(methodName, paramsClass);
		} catch (Exception e1) {
			for(; clazz != Object.class; clazz = clazz.getSuperclass()) {
				try {
					return clazz.getDeclaredMethod(methodName, paramsClass);
				} catch (Exception e) {
				}
			}
		}
        return method;
    }

	public static String getGetterName(String property) {
		if ((property == null) || (property.length() == 0)) {
			return "";
		}
		String str = "get" + property.substring(0, 1).toUpperCase();
		if (property.length() > 1) {
			str = str + property.substring(1);
		}
		return str;
	}

	/**
	 * 获取实例
	 * 
	 * @param clazz
	 * @return
	 */
	public static Object newInstance(Class<?> clazz){
		try {
			return clazz.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}
