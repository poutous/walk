package org.walkframework.batis.tools.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.Configuration;
import org.walkframework.batis.bean.EntityBoundSql;
import org.walkframework.batis.bean.OperColumnBean;
import org.walkframework.batis.dialect.Dialect;
import org.walkframework.batis.exception.NoUpdateColumnException;
import org.walkframework.data.annotation.Table;
import org.walkframework.data.entity.Condition;
import org.walkframework.data.entity.Conditions;
import org.walkframework.data.entity.Entity;
import org.walkframework.data.entity.EntityHelper;
import org.walkframework.data.entity.OperColumn;
import org.walkframework.data.entity.OperColumnHelper;
import org.walkframework.data.enums.SQLSymbol;
import org.walkframework.data.exception.ConditionEmptyException;
import org.walkframework.data.exception.ConditionValueIsNullException;
import org.walkframework.data.exception.EmptyEntityException;

/**
 * @author shf675
 * 
 */
public class SQlGenerator {

	public final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";

	private Dialect dialect;

	public SQlGenerator(Dialect dialect) {
		this.dialect = dialect;
	}

	/**
	 * generate insert sql
	 * 
	 * @param tableName
	 * @param namestr
	 * @param valuestr
	 * @return String
	 */
	@SuppressWarnings("unchecked")
	public EntityBoundSql generateInsertSql(Configuration configuration, Entity entity) {
		Map<String, OperColumn> operColumns = getOperColumns(entity, false).getOperColumns();
		List<ParameterMapping> parameterMappings = new ArrayList<ParameterMapping>();

		StringBuilder sql1 = new StringBuilder();
		StringBuilder sql2 = new StringBuilder();
		sql1.append("INSERT INTO " + getTableName(entity) + " (");
		sql2.append(") VALUES (");

		for (Map.Entry<String, OperColumn> entry : operColumns.entrySet()) {
			String column = entry.getKey();
			OperColumn operColumn = entry.getValue();
			if (OperColumnHelper.getOperColumnValue(operColumn) != null) {
				sql1.append(column).append(",");
				sql2.append("?").append(",");

				// 添加参数映射
				ParameterUtil.addParameterMapping(configuration, parameterMappings, OperColumnHelper.getOperColumnProperty(operColumn), OperColumnHelper.getOperColumnType(operColumn));
			}
		}
		sql1.deleteCharAt(sql1.length() - 1);
		sql2.deleteCharAt(sql2.length() - 1);
		sql1.append(sql2).append(")");
		return new EntityBoundSql(sql1.toString(), parameterMappings);
	}

	/**
	 * generate update sql
	 * 
	 * @param tableName
	 * @param setstr
	 * @param keystr
	 * @return String
	 */
	@SuppressWarnings("unchecked")
	public EntityBoundSql generateUpdateSql(Configuration configuration, Entity entity) {
		Map<String, OperColumn> operColumns = getOperColumns(entity, true).getOperColumns();
		List<ParameterMapping> parameterMappings = new ArrayList<ParameterMapping>();

		StringBuilder sql1 = new StringBuilder();
		StringBuilder sql2 = new StringBuilder();
		sql1.append("UPDATE " + getTableName(entity) + " SET ");
		sql2.append(" WHERE 1 = 1");

		// 更新语句必须设置条件

		// 1）先处理要更新的值
		int updateColumn = 0;
		for (Map.Entry<String, OperColumn> entry : operColumns.entrySet()) {
			String column = entry.getKey();
			OperColumn operColumn = entry.getValue();
			if (!OperColumnHelper.isCondition(operColumn)) {
				if (OperColumnHelper.getOperColumnValue(operColumn) != null) {
					sql1.append(column).append(" = ?,");

					// 添加参数映射
					ParameterUtil.addParameterMapping(configuration, parameterMappings, OperColumnHelper.getOperColumnProperty(operColumn), OperColumnHelper.getOperColumnType(operColumn));
				} else {
					sql1.append(column).append(" = NULL,");
				}
				updateColumn++;
			}
		}
		if (updateColumn == 0) {
			throw new NoUpdateColumnException();
		}

		// 2）条件处理
		for (Map.Entry<String, OperColumn> entry : operColumns.entrySet()) {
			OperColumn operColumn = entry.getValue();
			if (OperColumnHelper.isCondition(operColumn)) {
				conditionHandler(sql2, operColumn, configuration, parameterMappings);
			}
		}
		sql1.deleteCharAt(sql1.length() - 1);
		sql1.append(sql2);
		return new EntityBoundSql(sql1.toString(), parameterMappings);
	}

	/**
	 * generate delete sql
	 * 
	 * @param tableName
	 * @param setstr
	 * @param keystr
	 * @return String
	 */
	public EntityBoundSql generateDeleteSql(Configuration configuration, Entity entity) {
		OperColumnBean operColumnBean = getOperColumns(entity, true);
		Map<String, OperColumn> operColumns = operColumnBean.getOperColumns();
		List<ParameterMapping> parameterMappings = new ArrayList<ParameterMapping>();

		StringBuilder sql = new StringBuilder();
		sql.append("DELETE FROM " + getTableName(entity));
		sql.append(" WHERE 1 = 1");
		for (Map.Entry<String, OperColumn> entry : operColumns.entrySet()) {
			OperColumn operColumn = entry.getValue();
			// 条件处理
			if (OperColumnHelper.isCondition(operColumn)) {
				conditionHandler(sql, operColumn, configuration, parameterMappings);
			}
		}
		return new EntityBoundSql(sql.toString(), parameterMappings);
	}

	/**
	 * generate query sql
	 * 
	 * @param tableName
	 * @param setstr
	 * @param keystr
	 * @return String
	 */
	public EntityBoundSql generateSelectSql(Configuration configuration, Entity entity) {
		OperColumnBean operColumnBean = getOperColumns(entity, true);
		Map<String, OperColumn> operColumns = operColumnBean.getOperColumns();
		List<ParameterMapping> parameterMappings = new ArrayList<ParameterMapping>();

		StringBuilder sql1 = new StringBuilder();
		StringBuilder sql2 = new StringBuilder();
		sql1.append("SELECT *");
		sql1.append(" FROM " + getTableName(entity));
		sql1.append(" WHERE 1 = 1");

		sql2.append(" ORDER BY ");

		boolean hasOrderBy = false;
		if(operColumns != null){
			for (Map.Entry<String, OperColumn> entry : operColumns.entrySet()) {
				String column = entry.getKey();
				OperColumn operColumn = entry.getValue();
				// 条件处理
				if (OperColumnHelper.isCondition(operColumn)) {
					conditionHandler(sql1, operColumn, configuration, parameterMappings);
				}
				
				// 排序处理
				String sort = OperColumnHelper.getSort(operColumn);
				if (sort != null) {
					sql2.append(column).append(" ").append(sort).append(",");
					hasOrderBy = true;
				}
			}
		}

		if (hasOrderBy) {
			sql2.deleteCharAt(sql2.length() - 1);
			sql1.append(sql2);
		}
		return new EntityBoundSql(sql1.toString(), parameterMappings);
	}

	/**
	 * 获取实体操作的列
	 * 
	 * @param entity
	 * @param assertUpdateCondition
	 * @return
	 */
	private OperColumnBean getOperColumns(Entity entity, boolean assertCondition) {
		Map<String, OperColumn> operColumns = null;
		if (entity == null) {
			throw new EmptyEntityException();
		}
		operColumns = EntityHelper.operColumns(entity);
		if (operColumns == null || operColumns.keySet().size() == 0) {
			if(assertCondition){
				throw new EmptyEntityException();
			}
		}
		boolean hasCondition = false;
		if(operColumns != null){
			for (Map.Entry<String, OperColumn> entry : operColumns.entrySet()) {
				if (OperColumnHelper.isCondition(entry.getValue())) {
					hasCondition = true;
					break;
				}
			}
		}
		boolean isNoAnyCondition = EntityHelper.isNoAnyCondition(entity);
		if (!isNoAnyCondition && assertCondition && !hasCondition) {
			throw new ConditionEmptyException();
		}
		return new OperColumnBean(operColumns, hasCondition);
	}

	/**
	 * 获取表名
	 * 
	 * @param clazz
	 * @return
	 */
	public String getTableName(Entity entity) {
		if (Conditions.class.isAssignableFrom(entity.getClass())) {
			return EntityUtil.findEntity(EntityHelper.getEntityClazz((Conditions) entity)).getAnnotation(Table.class).name();
		}
		return EntityUtil.findEntity(entity.getClass()).getAnnotation(Table.class).name();
	}

	/**
	 * 条件处理
	 * 
	 * @param sql
	 * @param condition
	 * @param values
	 * @param configuration
	 * @param parameterMappings
	 * @param fields
	 */
	private void conditionHandler(StringBuilder sql, OperColumn operColumn, Configuration configuration, List<ParameterMapping> parameterMappings) {
		Condition condition = OperColumnHelper.getCondition(operColumn);
		sql.append(" AND ").append(condition.getColumn());
		// 检查条件值是否为null
		String symbol = condition.getSymbol();
		if (SQLSymbol.IS_NULL.value.equals(symbol) || SQLSymbol.NOT_NULL.value.equals(symbol)) {
			sql.append(symbol);
		} else {
			if (condition.isPre()) {
				if (OperColumnHelper.getOperColumnValue(operColumn) == null) {
					// 预处理模式如果值为null，表示IS NULL
					sql.append(SQLSymbol.IS_NULL.value);
				} else {
					sql.append(symbol).append("?");

					// 添加参数映射
					ParameterUtil.addParameterMapping(configuration, parameterMappings, OperColumnHelper.getOperColumnProperty(operColumn), OperColumnHelper.getOperColumnType(operColumn));
				}
			} else {
				if (SQLSymbol.IN.value.equals(symbol) || SQLSymbol.NOT_IN.value.equals(symbol)) {
					sql.append(symbol).append("(").append(inValueHandler(condition.getColumn(), condition.getValues())).append(")");
				} else if (SQLSymbol.BETWEEN.value.equals(symbol) || SQLSymbol.NOT_BETWEEN.value.equals(symbol)) {
					String[] retValues = betweenValueHandler(condition.getColumn(), condition.getValues());
					sql.append(symbol).append(retValues[0]).append(" AND ").append(retValues[1]);
				} else {
					Object[] conValues = condition.getValues();
					if (conValues == null || conValues.length == 0) {
						throw new ConditionValueIsNullException(condition.getColumn());
					}
					sql.append(symbol).append(valueHandler(condition.getColumn(), condition.getValues()[0]));
				}
			}
		}
	}

	/**
	 * between语句value处理
	 * 
	 * @param condition
	 * @param values
	 * @return
	 */
	private String[] betweenValueHandler(String condition, Object[] values) {
		if (values == null || values.length != 2) {
			throw new ConditionValueIsNullException(condition);
		}
		String[] retValues = new String[2];
		for (int i = 0; i < values.length; i++) {
			retValues[i] = valueHandler(condition, values[i]);
		}
		return retValues;
	}

	/**
	 * in语句value处理
	 * 
	 * @param condition
	 * @param values
	 * @return
	 */
	private String inValueHandler(String condition, Object[] values) {
		if (values == null || values.length == 0) {
			throw new ConditionValueIsNullException(condition);
		}
		StringBuilder inValues = new StringBuilder();
		for (int i = 0; i < values.length; i++) {
			inValues.append(valueHandler(condition, values[i])).append(",");
		}
		if (inValues.length() > 1) {
			inValues.deleteCharAt(inValues.length() - 1);
		}
		return inValues.toString();
	}

	/**
	 * value处理
	 * 
	 * @param condition
	 * @param value
	 * @return
	 */
	private String valueHandler(String condition, Object value) {
		String retValue = "";
		if (value == null) {
			throw new ConditionValueIsNullException(condition);
		}

		if (value instanceof Date) {
			retValue = this.dialect.getToDate(new SimpleDateFormat(DEFAULT_PATTERN, Locale.getDefault()).format(value));
		} else {
			retValue = "'" + value.toString() + "'";
		}
		return retValue;
	}
}
