package org.walkframework.batis.tools.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.walkframework.batis.constants.EntitySQL;
import org.walkframework.batis.dao.Dao;
import org.walkframework.batis.dialect.MySQLDialect;
import org.walkframework.data.annotation.Column;
import org.walkframework.data.annotation.Table;
import org.walkframework.data.entity.BaseEntity;
import org.walkframework.data.entity.Entity;
import org.walkframework.data.entity.EntityHelper;
import org.walkframework.data.entity.OperColumn;
import org.walkframework.data.entity.OperColumnHelper;
import org.walkframework.data.exception.EmptyEntityException;
import org.walkframework.data.exception.NoColumnException;
import org.walkframework.data.exception.NotEntityException;

/**
 * @author shf675
 *
 */
public abstract class EntityUtil {
	protected static final Logger log = LoggerFactory.getLogger(EntityUtil.class);
	
	/**
	 * 判断是否存在某字段
	 * 
	 * @param condition
	 */
	public static void assertExistField(Object object, String field){
		Class<?> entityClazz = EntityHelper.findEntity(object.getClass());
		try {
			entityClazz.getDeclaredField(field);
		} catch (NoSuchFieldException e) {
			throw new NoColumnException(entityClazz.getAnnotation(Table.class).name(), field);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	/** 
	 * 获取目标对象所有以@Column标注的字段，自上而下，即子类如果也定义了与父类相同的字段则以子类为准。
	 * 
	 * @param object : 对象 
	 */
	public static Map<String, Field> getAllColumnFields(Class<?> clazz) {
		return EntityHelper.getAllColumnFields(clazz);
	}
	
	/**
	 * 根据@Column标注的字段获取实体对象所有值
	 * 
	 * @param entity
	 * @return
	 */
	public static Map<String, Object> getAllValuesByColumn(Entity entity) {
		Map<String, Object> values = new HashMap<String, Object>();
		MetaObject entityMeta = SystemMetaObject.forObject(entity);
		Map<String, Field> fields = getAllColumnFields(entity.getClass());
		for (String column : fields.keySet()) {
			values.put(column, entityMeta.getValue(fields.get(column).getName()));
		}
		return values;
	}
	
	/**
	 * 处理操作列
	 * 
	 * @param entity
	 * @param columns
	 */
	public static void handleOperColumn(BaseEntity entity, String... conditionColumns) {
		if (conditionColumns != null && conditionColumns.length > 0) {
			Map<String, OperColumn> operColumns = EntityHelper.operColumns(entity);
			if (operColumns == null || operColumns.keySet().size() == 0) {
				throw new EmptyEntityException();
			}
			// 如果指定了条件，则取消原有实体设置的条件
			for (String column : operColumns.keySet()) {
				OperColumn operColumn = operColumns.get(column);
				if(OperColumnHelper.isCondition(operColumn)){
					OperColumnHelper.cancelCondition(operColumn);
				}
			}
			
			//重新设置条件
			MetaObject entityMeta = SystemMetaObject.forObject(entity);
			Map<String, Field> fields = EntityUtil.getAllColumnFields(entity.getClass());
			for (String column : conditionColumns) {
				Field field = fields.get(column);
				if (field == null) {
					throw new NoColumnException(entity.getClass().getAnnotation(Table.class).name(), column);
				}
				String property = field.getName();
				Object value = entityMeta.getValue(property);
				Class<?> type = field.getType();
				OperColumn operColumn = operColumns.get(column);
				if (operColumn == null) {
					operColumns.put(column, new OperColumn(column, property, value, type).asCondition());
				} else {
					operColumn.asCondition();
				}
			}
		}
	}
	
	/**
	 * 查找自增字段
	 * 
	 * 从当前实体类至父类依次查找自增字段，找到第一个发现的即停止查找
	 * @param clazz
	 * @return
	 */
	public static String findAutoIncrementPropertyName(Class<?> clazz){
		List<Class<?>> clazzs = new ArrayList<Class<?>>();
		//循环添加父类
		Class<?> currentClazz = clazz;
		for (; currentClazz != BaseEntity.class; currentClazz = currentClazz.getSuperclass()) {
			clazzs.add(currentClazz);
		}
		
		//从当前实体类至父类依次查找自增字段，找到第一个发现的即停止查找
		for (Class<?> clz : clazzs) {
			Field[] fields = clz.getDeclaredFields();
			for (Field field : fields) {
				Column columnAnn = field.getAnnotation(Column.class);
				if (columnAnn != null && columnAnn.isAutoIncrement()) {
					return field.getName();
				}
			}
		}
		return null;
	}
	
	/**
	 * 处理mysql的自增字段
	 * 
	 * @param entity
	 */
	public static void handleAutoIncrement(BaseEntity entity, Dao dao){
		//如果是mysql，设置自增字段值后返回
		if(dao.getDialect() instanceof MySQLDialect){
			
			//查找自增字段
			String autoIncrementPropertyName = EntityUtil.findAutoIncrementPropertyName(entity.getClass());
			
			//设置自增字段的值
			if(!StringUtils.isEmpty(autoIncrementPropertyName)){
				MetaObject entityMeta = SystemMetaObject.forObject(entity);
				Object originalValue = entityMeta.getValue(autoIncrementPropertyName);
				
				//自增字段如果手动设置了不为空的值，则不返回自增值
				if(StringUtils.isEmpty(originalValue)){
					Class<?> getterType = entityMeta.getGetterType(autoIncrementPropertyName);
					String lastId = dao.selectOne(EntitySQL.MYSQL_GET_LAST_INSERT_ID);
					Object value = EntityHelper.parseString(lastId, getterType);
					entityMeta.setValue(autoIncrementPropertyName, value);
				}
			}
		}
	}
}
