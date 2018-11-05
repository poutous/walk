package org.walkframework.fusioncharts.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.walkframework.fusioncharts.i18n.FusionChartsMessage;


public class ReflectionUtils {
	public static Object newInstance(String className, Object[] initargs) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
		Class clazz = Class.forName(className);
		return newInstance(clazz, initargs);
	}

	public static Object newInstance(Class clazz, Object[] initargs) throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Class[] parameterTypes = new Class[initargs.length];

		for (int i = 0; i < parameterTypes.length; i++) {
			parameterTypes[i] = initargs[i].getClass();
		}

		Constructor constructor = clazz.getConstructor(parameterTypes);
		return constructor.newInstance(initargs);
	}

	public static void loadClass(String className) throws ClassNotFoundException {
		loadClass(className, false);
	}

	public static void loadClass(String className, boolean initialize) throws ClassNotFoundException {
		Class.forName(className, initialize, ClassLoader.getSystemClassLoader());
	}

	public static Method getMethodByWholeName(Object obj, String methodName) throws Exception {
		Method[] methods = obj.getClass().getMethods();
		Method execMethod = null;
		int leftIndex = methodName.indexOf("(");
		int rightIndex = methodName.indexOf(")");
		if (leftIndex + 1 == rightIndex) {
			methodName = methodName.substring(0, leftIndex) + "\\(\\)";
			methodName = ".*" + methodName + ".*";
		} else {
			String middle = methodName.substring(leftIndex + 1, rightIndex);
			String[] params = middle.split(",");
			middle = "";
			for (int i = 0; i < params.length; i++) {
				middle = middle + ".*" + params[i] + ".*,";
			}
			middle = middle.substring(0, middle.length() - 1);
			methodName = ".*" + methodName.substring(0, leftIndex) + "\\(" + middle + "\\).*";
		}
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].toString().matches("^" + methodName + "$")) {
				execMethod = methods[i];
			}
		}

		if (execMethod == null) {
			throw new Exception(FusionChartsMessage.get("MethodUnfined", new Object[] { obj.getClass().getCanonicalName(), methodName }));
		}
		return execMethod;
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

	public static String getSetterName(String property) {
		if ((property == null) || (property.length() == 0)) {
			return "";
		}
		String str = "set" + property.substring(0, 1).toUpperCase();
		if (property.length() > 1) {
			str = str + property.substring(1);
		}
		return str;
	}

	public static void setFieldValue(Object target, String property, Class propertyType, Object propertyValue) throws Exception {
		if ((target == null) || (property == null) || ("".equals(property))) {
			return;
		}
		Class clazz = target.getClass();

		String setterName = getSetterName(property);

		Method method = null;

		if (propertyType == null) {
			Method[] methods = clazz.getDeclaredMethods();
			for (int i = 0; i < methods.length; i++) {
				if (methods[i].getName().equals(setterName)) {
					method = methods[i];
					break;
				}
			}
		} else {
			method = clazz.getDeclaredMethod(setterName, new Class[] { propertyType });
		}

		method.invoke(target, new Object[] { propertyValue });
	}

	public static Object getFieldValue(Object target, String property) throws Exception {
		if ((target == null) || (property == null) || ("".equals(property))) {
			return null;
		}
		if(target instanceof Map){
			return ((Map)target).get(property);
		}

		Class clazz = target.getClass();
		String getterName = getGetterName(property);
		Method method = clazz.getDeclaredMethod(getterName, new Class[0]);
		return method.invoke(target, new Object[0]);
	}
}