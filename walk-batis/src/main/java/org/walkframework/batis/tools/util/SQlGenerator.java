package org.walkframework.batis.tools.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.Configuration;
import org.walkframework.batis.bean.EntityBoundSql;
import org.walkframework.batis.bean.OperColumnBean;
import org.walkframework.batis.exception.NoUpdateColumnException;
import org.walkframework.data.annotation.Table;
import org.walkframework.data.entity.Conditions;
import org.walkframework.data.entity.Entity;
import org.walkframework.data.entity.EntityHelper;
import org.walkframework.data.entity.OperColumn;
import org.walkframework.data.entity.OperColumnHelper;
import org.walkframework.data.enums.SQLSymbol;
import org.walkframework.data.exception.ConditionEmptyException;
import org.walkframework.data.exception.EmptyEntityException;

/**
 * @author shf675
 * 
 */
public class SQlGenerator {

	public final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";

	/**
	 * generate insert sql
	 * 
	 * @param tableName
	 * @param namestr
	 * @param valuestr
	 * @return String
	 */
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
		
		boolean isNoAnyCondition = EntityHelper.isNoAnyCondition(entity);
		operColumns = EntityHelper.operColumns(entity);
		if (operColumns == null || operColumns.keySet().size() == 0) {
			if(assertCondition && !isNoAnyCondition){
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
		return EntityHelper.findEntity(entity.getClass()).getAnnotation(Table.class).name();
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
		sql.append(" AND ").append(OperColumnHelper.getOperColumn(operColumn));
		if (OperColumnHelper.getOperColumnValue(operColumn) == null) {
			// 预处理模式如果值为null，表示IS NULL
			sql.append(SQLSymbol.IS_NULL.value);
		} else {
			sql.append(SQLSymbol.EQUAL.value).append("?");
			
			// 添加参数映射
			ParameterUtil.addParameterMapping(configuration, parameterMappings, OperColumnHelper.getOperColumnProperty(operColumn), OperColumnHelper.getOperColumnType(operColumn));
		}
	}
}
