package org.walkframework.data.entity;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.walkframework.data.annotation.Table;
import org.walkframework.data.exception.EntityClassIsNullException;
import org.walkframework.data.exception.NoColumnException;


/**
 * 动态拼条件
 * 
 * @author shf675
 *
 */
public class Conditions extends AbstractEntity {
	
	private static final long serialVersionUID = 1L;
	
	private static final String SEP = "__";
	
	private Class<? extends BaseEntity> entityClazz;
	
	private Map<String, Field> fields;
	
	private Map<String, Object> parameters;
	
	private StringBuilder sql = new StringBuilder();
	private StringBuilder orderSql = new StringBuilder();
	
	private StringBuilder extendSql = new StringBuilder();
	
	public Conditions(Class<? extends BaseEntity> entityClazz){
		if(entityClazz == null){
			throw new EntityClassIsNullException();
		}
		this.entityClazz = entityClazz;
		fields = EntityHelper.getAllColumnFields(entityClazz);
		parameters = new HashMap<String, Object>();
		sql = new StringBuilder();
		
		//sql初始化
		sql.append("SELECT *");
		sql.append(" FROM " + EntityHelper.findEntity(entityClazz).getAnnotation(Table.class).name());
		sql.append(" WHERE 1 = 1");

	}
	
	public Conditions andEqual(String column, Object value) {
		Condition condition = addCondition(column);
		condition.andEqual(value);
		addParameterAndSQL(condition);
		return this;
	}
	
	public Conditions andNotEqual(String column, Object value) {
		Condition condition = addCondition(column);
		condition.andNotEqual(value);
		addParameterAndSQL(condition);
		return this;
	}

	public Conditions andLike(String column, Object value) {
		Condition condition = addCondition(column);
		condition.andLike(value);
		addParameterAndSQL(condition);
		return this;
	}

	public Conditions andNotLike(String column, Object value) {
		Condition condition = addCondition(column);
		condition.andNotLike(value);
		addParameterAndSQL(condition);
		return this;
	}

	public Conditions andGreater(String column, Object value) {
		Condition condition = addCondition(column);
		condition.andGreater(value);
		addParameterAndSQL(condition);
		return this;
	}

	public Conditions andGreaterEqual(String column, Object value) {
		Condition condition = addCondition(column);
		condition.andGreaterEqual(value);
		addParameterAndSQL(condition);
		return this;
	}

	public Conditions andLess(String column, Object value) {
		Condition condition = addCondition(column);
		condition.andLess(value);
		addParameterAndSQL(condition);
		return this;
	}

	public Conditions andLessEqual(String column, Object value) {
		Condition condition = addCondition(column);
		condition.andLessEqual(value);
		addParameterAndSQL(condition);
		return this;
	}

	public Conditions andIsNull(String column) {
		Condition condition = addCondition(column);
		condition.andIsNull();
		addParameterAndSQL(condition);
		return this;
	}

	public Conditions andIsNotNull(String column) {
		Condition condition = addCondition(column);
		condition.andIsNotNull();
		addParameterAndSQL(condition);
		return this;
	}

	public Conditions andIn(String column, Object... values) {
		Condition condition = addCondition(column);
		condition.andIn(values);
		addParameterAndSQL(condition);
		return this;
	}

	public Conditions andNotIn(String column, Object... values) {
		Condition condition = addCondition(column);
		condition.andNotIn(values);
		addParameterAndSQL(condition);
		return this;
	}

	public Conditions andBetween(String column, Object value1, Object value2) {
		Condition condition = addCondition(column);
		condition.andBetween(value1, value2);
		addParameterAndSQL(condition);
		return this;
	}

	public Conditions andNotBetween(String column, Object value1, Object value2) {
		Condition condition = addCondition(column);
		condition.andNotBetween(value1, value2);
		addParameterAndSQL(condition);
		return this;
	}
	
	public Conditions addSql(String sql) {
		extendSql.append(sql);
		return this;
	}
	
	/**
	 * 添加升序排序字段
	 * 
	 * @param column
	 * @return
	 */
	public Conditions orderByAsc(String... columns){
		if(columns != null && columns.length > 0){
			for (String column : columns) {
				orderSql.append(column).append(" ASC,");
			}
		}
		return this;
	}
	
	/**
	 * 添加降序排序字段
	 * 
	 * @param column
	 * @return
	 */
	public Conditions orderByDesc(String... columns){
		if(columns != null && columns.length > 0){
			for (String column : columns) {
				orderSql.append(column).append(" DESC,");
			}
		}
		return this;
	}
	
	/**
	 * 添加条件
	 * 
	 * @param column
	 * @return
	 */
	private Condition addCondition(String column){
		Field field = fields.get(column);
		if(field == null){
			throw new NoColumnException(entityClazz.getAnnotation(Table.class).name(), column);
		}
		return new Condition(column);
	}
	
	/**
	 * 添加参数并且组装sql
	 * 
	 * @param condition
	 */
	private void addParameterAndSQL(Condition condition) {
		Object[] values = (Object[])condition.getValues();
		if(values == null || values.length == 0){
			if(!condition.getSymbol().name().matches("IS_NULL|NOT_NULL")) {
				return;
			}
		}
		String parameterKey = condition.getColumn() + SEP + condition.getSymbol().name();
		if(condition.getSymbol().name().matches("IN|NOT_IN")){
			sql.append(" AND ").append(condition.getColumn()).append(condition.getSymbol().value + "(");
			for (int i = 0; i < values.length; i++) {
				String key = parameterKey + SEP + i;
				parameters.put(key, values[i]);
				sql.append("#{").append(key).append("},");
			}
			sql.deleteCharAt(sql.length() - 1);
			sql.append(")");
		} else if(condition.getSymbol().name().matches("BETWEEN|NOT_BETWEEN")){
			String key0 = parameterKey + SEP + 0;
			String key1 = parameterKey + SEP + 1;
			parameters.put(key0, values[0]);
			parameters.put(key1, values[1]);
			sql.append(" AND ").append(condition.getColumn()).append(condition.getSymbol().value);
			sql.append("#{").append(key0).append("} AND ").append("#{").append(key1).append("}");
		} else if(condition.getSymbol().name().matches("IS_NULL|NOT_NULL")){
			sql.append(" AND ").append(condition.getColumn()).append(condition.getSymbol().value);
		} else {
			parameters.put(parameterKey, values[0]);
			sql.append(" AND ").append(condition.getColumn()).append(condition.getSymbol().value).append("#{").append(parameterKey).append("}");
		}
	}
	
	/**
	 * 获取实体class
	 * 
	 * @return
	 */
	Class<? extends BaseEntity> getEntityClazz() {
		return entityClazz;
	}
	
	Map<String, Object> getParameters() {
		return parameters;
	}
	
	StringBuilder getSql() {
		return sql.append(extendSql);
	}
	
	StringBuilder getOrderSql() {
		return orderSql;
	}
}
