package org.walkframework.batis.tools.util;

import java.util.List;

import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.session.Configuration;

/**
 * @author shf675
 *
 */
public abstract class ParameterUtil {
	
	/**
	 * 判断是否是String或基本类型包装类型
	 * @param clazz
	 * @return
	 */
	public static boolean isStringOrBasicType(Class<?> clazz){
		if(String.class.isAssignableFrom(clazz) || Number.class.isAssignableFrom(clazz) || Boolean.class.isAssignableFrom(clazz) || Character.class.isAssignableFrom(clazz)){
			return true;
		}
		return false;
	}
	
	/**
	 * 添加参数映射
	 * 
	 * @param configuration
	 * @param parameterMappings
	 * @param fields
	 * @param column
	 */
	public static void addParameterMapping(Configuration configuration, List<ParameterMapping> parameterMappings, String operColumnProperty, Class<?> operColumnType) {
		ParameterMapping.Builder builder = new ParameterMapping.Builder(configuration, operColumnProperty, operColumnType);
		builder.mode(ParameterMode.IN);
		parameterMappings.add(builder.build());
	}
}
