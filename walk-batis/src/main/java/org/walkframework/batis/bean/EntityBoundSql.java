package org.walkframework.batis.bean;

import java.util.List;

import org.apache.ibatis.mapping.ParameterMapping;


/**
 * @author shf675
 *
 */
public class EntityBoundSql {
	
	private String sql;
	
	private List<ParameterMapping> parameterMappings;

	public EntityBoundSql(String sql, List<ParameterMapping> parameterMappings){
		this.sql = sql;
		this.parameterMappings = parameterMappings;
	}
	
	public List<ParameterMapping> getParameterMappings() {
		return parameterMappings;
	}

	public String getSql() {
		return sql;
	}
}
