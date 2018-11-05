package org.walkframework.data.entity;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.walkframework.data.annotation.Table;
import org.walkframework.data.exception.EntityClassIsNullException;
import org.walkframework.data.exception.NoColumnException;

import com.alibaba.fastjson.JSON;


/**
 * 动态拼条件
 * 
 * @author shf675
 *
 */
public class Conditions extends AbstractEntity {
	
	private static final long serialVersionUID = 1L;
	
	private Class<? extends BaseEntity> entityClazz;
	
	private Map<String, Field> fields;
	
	public Conditions(Class<? extends BaseEntity> entityClazz){
		if(entityClazz == null){
			throw new EntityClassIsNullException();
		}
		this.entityClazz = entityClazz;
		fields = EntityHelper.getAllColumnFields(entityClazz);
	}

	/**
	 * 添加条件
	 * 
	 * @param column
	 * @return
	 */
	public Condition addCondition(String column){
		Field field = fields.get(column);
		if(field == null){
			throw new NoColumnException(entityClazz.getAnnotation(Table.class).name(), column);
		}
		OperColumn operColumn = addOperColumn(column, field.getName(), null, field.getType());
		operColumn.asCondition();
		return operColumn.getCondition();
	}
	
	/**
	 * 添加升序排序字段
	 * 
	 * @param column
	 * @return
	 */
	public Conditions addOrderByAsc(String column){
		Field field = fields.get(column);
		OperColumn operColumn = addOperColumn(column, field.getName(), null, field.getType());
		operColumn.asOrderByAsc();
		return this;
	}
	
	/**
	 * 添加降序排序字段
	 * 
	 * @param column
	 * @return
	 */
	public Conditions addOrderByDesc(String column){
		Field field = fields.get(column);
		OperColumn operColumn = addOperColumn(column, field.getName(), null, field.getType());
		operColumn.asOrderByDesc();
		return this;
	}
	
	/**
	 * 获取实体class
	 * 
	 * @return
	 */
	Class<? extends BaseEntity> getEntityClazz() {
		return entityClazz;
	}
	
	@Override
	public String toString() {
		Map<String, Object> kv = new HashMap<String, Object>();
		for (String column : operColumns().keySet()) {
			kv.put(column, operColumns().get(column).getCondition().getValues());
		}
		return JSON.toJSONString(kv, serializerFeatures);
	}
}
