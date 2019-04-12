package org.walkframework.activiti.system.process;



/**
 * 启动流程入参
 * 
 * @author shf675
 */
public class StartEntity {

	/**
	 * 流程标识 （流程定义的key）。必填
	 */
	private String procDefKey;
	
	/**
	 * 业务ID。必填
	 */
	private String businessId;
	
	/**	
	 * 工单发起人。必填
	 */	
	private String submitor;
	
	/**
	 * 业务描述。选填
	 */
	private String businessDesc;
	
	/**
	 * 构造函数
	 * 
	 * @param procDefKey 流程定义KEY。必填
	 * @param businessId 业务ID。必填
	 * @param submitor 工单发起人。必填
	 */
	public StartEntity(String procDefKey, String businessId, String submitor) {
		this.procDefKey = procDefKey;
		this.businessId = businessId;
		this.submitor = submitor;
	}
	
	public String getSubmitor() {
		return submitor;
	}

	public String getProcDefKey() {
		return procDefKey;
	}


	public String getBusinessId() {
		return businessId;
	}
	
	public String getBusinessDesc() {
		return businessDesc;
	}

	public StartEntity setBusinessDesc(String businessDesc) {
		this.businessDesc = businessDesc;
		return this;
	}
}
