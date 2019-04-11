package org.walkframework.activiti.system.process;


/**
 * 回写业务表入参
 * @author shf675
 */
public class WriteBackEntity {
	
	/**	
	 * 业务表表名
	 */	
	private String businessTable;
	
	/**	
	 * 业务表ID字段
	 */	
	private String businessIdPrimaryKey;
	
	/**	
	 * 业务ID
	 */	
	private String businessId;

	/**	
	 * 流程定义KEY	
	 */	
	private String procDefKey;

	/**	
	 * 流程实例ID	
	 */	
	private String procInstId;

	/**	
	 * 流程当前状态	
	 */	
	private String procState;
	
	/**	
	 * 流程当前结点	
	 */	
	private String procTaskDefKey;
	
	/**	
	 * 操作人
	 */	
	private String operator;
	
	public String getBusinessTable() {
		return businessTable;
	}

	public void setBusinessTable(String businessTable) {
		this.businessTable = businessTable;
	}
	
	public String getBusinessIdPrimaryKey() {
		return businessIdPrimaryKey;
	}

	public void setBusinessIdPrimaryKey(String businessIdPrimaryKey) {
		this.businessIdPrimaryKey = businessIdPrimaryKey;
	}

	public String getBusinessId() {
		return businessId;
	}

	public void setBusinessId(String businessId) {
		this.businessId = businessId;
	}
	
	public String getProcDefKey() {
		return procDefKey;
	}

	public void setProcDefKey(String procDefKey) {
		this.procDefKey = procDefKey;
	}

	public String getProcInstId() {
		return procInstId;
	}

	public void setProcInstId(String procInstId) {
		this.procInstId = procInstId;
	}

	public String getProcState() {
		return procState;
	}

	public void setProcState(String procState) {
		this.procState = procState;
	}

	public String getProcTaskDefKey() {
		return procTaskDefKey;
	}

	public void setProcTaskDefKey(String procTaskDefKey) {
		this.procTaskDefKey = procTaskDefKey;
	}
	
	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}
}
