package org.walkframework.batis.bean;

import java.util.Map;

import org.walkframework.data.entity.OperColumn;

/**
 * @author shf675
 * 
 */
public class OperColumnBean {

	private Map<String, OperColumn> operColumns;

	private boolean hasCondition;

	public OperColumnBean(Map<String, OperColumn> operColumns, boolean hasCondition) {
		this.operColumns = operColumns;
		this.hasCondition = hasCondition;
	}

	public Map<String, OperColumn> getOperColumns() {
		return operColumns;
	}

	public boolean isHasCondition() {
		return hasCondition;
	}
}
