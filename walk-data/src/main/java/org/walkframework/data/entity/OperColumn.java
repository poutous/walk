package org.walkframework.data.entity;

import java.io.Serializable;

/**
 * @author shf675
 *
 */
public class OperColumn implements Serializable {
	private static final long serialVersionUID = 1L;

	private String operColumn;
	private String operColumnProperty;
	private Object operColumnValue;
	private Class<?> operColumnType;
	
	private String sort;
	private boolean isCondition;
	
	public OperColumn(String operColumn, String operColumnProperty, Object operColumnValue, Class<?> operColumnType) {
		this.operColumn = operColumn;
		this.operColumnProperty = operColumnProperty;
		this.operColumnValue = operColumnValue;
		this.operColumnType = operColumnType;
	}

	/**
	 * 作为条件
	 * @return
	 */
	public OperColumn asCondition() {
		this.isCondition = true;
		return this;
	}

	/**
	 * 作为排序字段
	 * 升序
	 * @return
	 */
	public OperColumn asOrderByAsc() {
		this.sort = "ASC";
		return this;
	}

	/**
	 * 作为排序字段
	 * 降序
	 * @return
	 */
	public OperColumn asOrderByDesc() {
		this.sort = "DESC";
		return this;
	}

	String getOperColumn() {
		return operColumn;
	}
	
	void setIsCondition(boolean isCondition) {
		this.isCondition = isCondition;
	}
	
	void cancelCondition() {
		setIsCondition(false);
	}

	boolean isCondition() {
		return isCondition;
	}
	
	String getSort() {
		return sort;
	}

	Object getOperColumnValue() {
		return operColumnValue;
	}

	Class<?> getOperColumnType() {
		return operColumnType;
	}

	String getOperColumnProperty() {
		return operColumnProperty;
	}

	void setOperColumnValue(Object operColumnValue) {
		this.operColumnValue = operColumnValue;
	}
}
