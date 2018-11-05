package org.walkframework.base.system.staticparam;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

/**
 * @author shf675
 * 
 */
public class StaticParam implements Serializable {

	private static final long serialVersionUID = 1L;

	private String key;

	private String primaryKey;

	private String condition;

	private String sqlId;

	private boolean load = true;
	
	private String managerId;

	public boolean isLoad() {
		return load;
	}

	public void setLoad(boolean load) {
		this.load = load;
	}

	public String getSqlId() {
		return StringUtils.trim(sqlId);
	}

	public void setSqlId(String sqlId) {
		this.sqlId = sqlId;
	}

	public String getPrimaryKey() {
		return StringUtils.trim(primaryKey);
	}

	public void setPrimaryKey(String primaryKey) {
		this.primaryKey = primaryKey;
	}

	public String getCondition() {
		return StringUtils.trim(condition);
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public String getKey() {
		return StringUtils.trim(key);
	}

	public void setKey(String key) {
		this.key = key;
	}
	
	@Override
	public String toString() {
		StringBuilder string = new StringBuilder();
		if (StringUtils.isNoneEmpty(getKey()))
			string.append("key=").append(getKey()).append(";");
		if (StringUtils.isNoneEmpty(getPrimaryKey()))
			string.append("primaryKey=").append(getPrimaryKey()).append(";");
		if (StringUtils.isNoneEmpty(getCondition()))
			string.append("condition=").append(getCondition()).append(";");
		if (StringUtils.isNoneEmpty(getSqlId()))
			string.append("sqlId=").append(getSqlId()).append(";");
		if (StringUtils.isNoneEmpty(getManagerId()))
			string.append("managerId=").append(getManagerId()).append(";");
		return string.toString();
	}

	public String getManagerId() {
		return managerId;
	}

	public void setManagerId(String managerId) {
		this.managerId = managerId;
	}
}
