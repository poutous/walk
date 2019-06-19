package org.walkframework.data.entity;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.walkframework.data.annotation.Column;
import org.walkframework.data.annotation.Table;
import org.walkframework.data.exception.NotEntityException;

/**
 * @author shf675
 * 
 */
public abstract class EntityHelper {
	private final static Logger log = LoggerFactory.getLogger(EntityHelper.class);
	
	@SuppressWarnings("unchecked")
	public static Class<? extends BaseEntity> getEntityClazz(Entity entity) {
		if (entity instanceof Conditions) {
			return ((Conditions) entity).getEntityClazz();
		} else if(entity.getClass().isAnonymousClass()){
			return (Class) entity.getClass().getSuperclass();
		}
		return ((BaseEntity) entity).getClass();
	}
	
	/**
	 * 查找实体类
	 * 
	 * @param clazz
	 * @return
	 */
	public static Class<?> findEntity(Class<?> clazz){
		if(clazz.getAnnotation(Table.class) != null){
			return clazz;
		} else {
			Class<?> superClazz = clazz.getSuperclass();
			if(Object.class.equals(superClazz)){
				throw new NotEntityException(clazz);
			}
			return findEntity(superClazz);
		}
	}
	
	/**
	 * 获取Conditions方式sql
	 * 
	 * @param conditions
	 * @return
	 */
	public static String getConditionsSql(Conditions conditions) {
		StringBuilder sql = conditions.getSql();
		StringBuilder orderSql = conditions.getOrderSql();
		if(orderSql.length() > 0){
			sql.append(" ORDER BY ").append(orderSql.deleteCharAt(orderSql.length() - 1));
		}
		return sql.toString();
	}
	
	public static Object getConditionsParameters(Conditions conditions) {
		return conditions.getParameters();
	}

	public static Map<String, OperColumn> operColumns(Entity entity) {
		return ((AbstractEntity)entity).operColumns();
	}
	
	public static boolean isNoAnyCondition(Entity entity) {
		return ((AbstractEntity)entity).isNoAnyCondition();
	}
	
	/** 
	 * 获取目标对象所有以@Column标注的字段，自上而下，即子类如果也定义了与父类相同的字段则以子类为准。
	 * 
	 * @param object : 对象 
	 */
	public static Map<String, Field> getAllColumnFields(Class<?> clazz) {
		Map<String, Field> allFields = new HashMap<String, Field>();
		List<Class<?>> clazzs = new ArrayList<Class<?>>();
		//循环添加父类
		for (; clazz != BaseEntity.class; clazz = clazz.getSuperclass()) {
			clazzs.add(clazz);
		}
		//反转
		Collections.reverse(clazzs);
		//添加字段
		for (Class<?> clz : clazzs) {
			Field[] fields = clz.getDeclaredFields();
			for (Field field : fields) {
				Column columnAnn = field.getAnnotation(Column.class);
				if (columnAnn != null) {
					allFields.put(columnAnn.name(), field);
				}
			}
		}
		return allFields;
	}
	
	/**
	 * map转实体类
	 * 
	 * @param <T>
	 * @param src
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T map2entity(Map src, Class<? extends BaseEntity> targetClazz){
		Object target = null;
		Class<?> currentClazz = targetClazz;
		try {
			target = targetClazz.newInstance();
			if(src != null && !src.isEmpty()){
				List<Class<?>> clazzs = new ArrayList<Class<?>>();
				//循环添加父类
				for (; currentClazz != BaseEntity.class; currentClazz = currentClazz.getSuperclass()) {
					clazzs.add(currentClazz);
				}
				//反转
				Collections.reverse(clazzs);
				//开始转换
				for (Class<?> clz : clazzs) {
					Field[] fields = clz.getDeclaredFields();
					for (Field field : fields) {
						Column columnAnn = field.getAnnotation(Column.class);
						if (columnAnn != null && src.containsKey(columnAnn.name())) {
							Object value = src.get(columnAnn.name());
							if (value != null && !value.getClass().equals(field.getType())) {
								if ((String.class.isAssignableFrom(field.getType()) || Number.class.isAssignableFrom(field.getType()) || Boolean.class.isAssignableFrom(field.getType())) 
										&& (String.class.isAssignableFrom(value.getClass()) || Number.class.isAssignableFrom(value.getClass()) || Boolean.class.isAssignableFrom(value.getClass()))) {
									value = parseString(value.toString(), field.getType());
								}
							}

							String methodName = "set" + toUpperCaseFirstOne(field.getName());
							targetClazz.getMethod(methodName, field.getType()).invoke(target, value);
						}
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return (T) target;
	}
	
	/**
	 * 实体类转map
	 * 
	 * key 为property
	 * @param <T>
	 * @param src 源对象
	 * @param targetClazz 目标对象class
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T entity2map(BaseEntity src, Class<? extends Map> targetClazz){
		return entity2map(src, targetClazz, false);
	}
	
	/**
	 * 实体类转map
	 * 
	 * @param <T>
	 * @param src 源对象
	 * @param targetClazz 目标对象class
	 * @param keyIsColumn 是否以数据库字段作为key
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T entity2map(BaseEntity src, Class<? extends Map> targetClazz, boolean keyIsColumn){
		Map target = null;
		Class<?> currentClazz = src.getClass();
		try {
			target = (Map)targetClazz.newInstance();
			if(src != null && src.operColumns() != null && !src.operColumns().isEmpty()){
				List<Class<?>> clazzs = new ArrayList<Class<?>>();
				//循环添加父类
				for (; currentClazz != BaseEntity.class; currentClazz = currentClazz.getSuperclass()) {
					clazzs.add(currentClazz);
				}
				//反转
				Collections.reverse(clazzs);
				//开始转换
				for (Class<?> clz : clazzs) {
					Field[] fields = clz.getDeclaredFields();
					for (Field field : fields) {
						Column columnAnn = field.getAnnotation(Column.class);
						if (columnAnn != null) {
							String methodName = "get" + toUpperCaseFirstOne(field.getName());
							String key = field.getName();
							if(keyIsColumn){
								key = columnAnn.name();
							}
							target.put(key, src.getClass().getMethod(methodName).invoke(src));
						}
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return (T) target;
	}
	
	/**
	 * 转换字符串形式的数字为真正的类型
	 * 
	 * @param value
	 * @param type
	 * @return
	 */
	public static Object parseString(String value, Class<?> type) {
		if (value == null || "".equals(value) || type == null) {
			return value;
		}
		if (int.class.equals(type)) {
			type = Integer.class;
		} else if (double.class.equals(type)) {
			type = Double.class;
		} else if (float.class.equals(type)) {
			type = Float.class;
		} else if (long.class.equals(type)) {
			type = Long.class;
		} else if (short.class.equals(type)) {
			type = Short.class;
		} else if (boolean.class.equals(type)) {
			type = Boolean.class;
		} else if (byte.class.equals(type)) {
			type = Byte.class;
		}
		if (Number.class.isAssignableFrom(type) || Boolean.class.isAssignableFrom(type)) {
			try {
				return type.getConstructor(String.class).newInstance(value);
			} catch (IllegalArgumentException e) {
				log.error(e.getMessage(), e);
			} catch (SecurityException e) {
				log.error(e.getMessage(), e);
			} catch (InstantiationException e) {
				log.error(e.getMessage(), e);
			} catch (IllegalAccessException e) {
				log.error(e.getMessage(), e);
			} catch (InvocationTargetException e) {
				log.error(e.getMessage(), e);
			} catch (NoSuchMethodException e) {
				log.error(e.getMessage(), e);
			}
		}
		return value;
	}
	
	/**
	 * 首字母大写
	 * 
	 * @param s
	 * @return
	 */
	private static String toUpperCaseFirstOne(String s) {
		if (Character.isUpperCase(s.charAt(0)))
			return s;
		else
			return (new StringBuilder()).append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).toString();
	}

	public String getUniqeName() {
		return String.valueOf(System.currentTimeMillis()) + Math.abs(new Random().nextInt());
	}
}
